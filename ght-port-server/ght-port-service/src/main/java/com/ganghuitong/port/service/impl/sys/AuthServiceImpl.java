package com.ganghuitong.port.service.impl.sys;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.context.LoginUser;
import com.ganghuitong.port.api.context.TenantContext;
import com.ganghuitong.port.api.context.UserContext;
import com.ganghuitong.port.api.dto.sys.ChangePwdReq;
import com.ganghuitong.port.api.dto.sys.LoginReq;
import com.ganghuitong.port.api.entity.sys.SysTenant;
import com.ganghuitong.port.api.entity.sys.SysUser;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.sys.AuthService;
import com.ganghuitong.port.api.service.sys.SysMenuService;
import com.ganghuitong.port.api.service.sys.TokenService;
import com.ganghuitong.port.api.util.PasswordUtil;
import com.ganghuitong.port.api.vo.sys.LoginResp;
import com.ganghuitong.port.api.vo.sys.TenantOptionVO;
import com.ganghuitong.port.api.vo.sys.UserInfoVO;
import com.ganghuitong.port.service.mapper.sys.SysTenantMapper;
import com.ganghuitong.port.service.mapper.sys.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录认证服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /**
     * 租户数据访问
     */
    private final SysTenantMapper sysTenantMapper;

    /**
     * 用户数据访问
     */
    private final SysUserMapper sysUserMapper;

    /**
     * 令牌服务
     */
    private final TokenService tokenService;

    /**
     * 菜单服务
     */
    private final SysMenuService sysMenuService;

    /**
     * 账号密码登录
     *
     * @param req 登录入参（租户编码、账号、密码）
     * @return 登录结果（令牌、昵称、租户名称）
     */
    @Override
    public LoginResp login(LoginReq req) {
        SysTenant tenant = sysTenantMapper.selectOne(Wrappers.<SysTenant>lambdaQuery()
                .eq(SysTenant::getTenantCode, req.getTenantCode()));
        if (tenant == null) {
            log.info("登录失败，租户编码[{}]不存在", req.getTenantCode());
            throw new CustException("所属港口不存在");
        }
        if (!StrUtil.equals(EnableStatusEnum.ENABLED.getCode(), tenant.getStatus())) {
            log.info("登录失败，租户[{}]已停用", tenant.getTenantCode());
            throw new CustException("该港口已停用，请联系平台管理员");
        }
        if (tenant.getExpireTime() != null && tenant.getExpireTime().isBefore(LocalDateTime.now())) {
            log.info("登录失败，租户[{}]已到期", tenant.getTenantCode());
            throw new CustException("该港口服务已到期，请联系平台管理员");
        }
        TenantContext.set(tenant.getId());
        try {
            SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                    .eq(SysUser::getUsername, req.getUsername()));
            if (user == null || !PasswordUtil.matches(req.getPassword(), user.getSalt(), user.getPassword())) {
                log.info("登录失败，账号[{}]不存在或密码错误", req.getUsername());
                throw new CustException("账号或密码错误");
            }
            if (!StrUtil.equals(EnableStatusEnum.ENABLED.getCode(), user.getStatus())) {
                log.info("登录失败，账号[{}]已停用", req.getUsername());
                throw new CustException("账号已停用");
            }
            SysUser update = new SysUser();
            update.setId(user.getId());
            update.setLastLoginTime(LocalDateTime.now());
            sysUserMapper.updateById(update);
            LoginResp resp = new LoginResp();
            resp.setToken(tokenService.createToken(user.getId(), tenant.getId()));
            resp.setNickname(user.getNickname());
            resp.setTenantName(tenant.getTenantName());
            log.info("账号[{}]登录租户[{}]成功", user.getUsername(), tenant.getTenantCode());
            return resp;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 获取当前登录用户信息（含角色、权限、菜单树）
     *
     * @return 当前用户信息
     */
    @Override
    public UserInfoVO currentUserInfo() {
        LoginUser user = UserContext.require();
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setUserType(user.getUserType());
        vo.setRefId(user.getRefId());
        vo.setTenantCode(user.getTenantCode());
        vo.setTenantName(user.getTenantName());
        vo.setRoles(user.getRoleCodes());
        vo.setPerms(user.getPerms());
        vo.setMenus(sysMenuService.userMenuTree(user));
        return vo;
    }

    /**
     * 修改当前用户密码
     *
     * @param req 修改密码入参（原密码、新密码）
     */
    @Override
    public void changePassword(ChangePwdReq req) {
        LoginUser loginUser = UserContext.require();
        SysUser user = sysUserMapper.selectById(loginUser.getUserId());
        if (user == null || !PasswordUtil.matches(req.getOldPassword(), user.getSalt(), user.getPassword())) {
            log.info("账号[{}]修改密码失败，原密码错误", loginUser.getUsername());
            throw new CustException("原密码错误");
        }
        String salt = PasswordUtil.randomSalt();
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setSalt(salt);
        update.setPassword(PasswordUtil.encrypt(req.getNewPassword(), salt));
        sysUserMapper.updateById(update);
    }

    /**
     * 查询登录页可选的租户（港口）列表
     *
     * @return 启用状态的租户选项
     */
    @Override
    public List<TenantOptionVO> tenantOptions() {
        return sysTenantMapper.selectList(Wrappers.<SysTenant>lambdaQuery()
                        .eq(SysTenant::getStatus, EnableStatusEnum.ENABLED.getCode())
                        .orderByAsc(SysTenant::getCreateTime))
                .stream()
                .map(t -> {
                    TenantOptionVO vo = new TenantOptionVO();
                    vo.setTenantCode(t.getTenantCode());
                    vo.setTenantName(t.getTenantName());
                    return vo;
                })
                .toList();
    }
}
