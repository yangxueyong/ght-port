package com.ganghuitong.port.service.impl.sys;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.dto.sys.AccountProvisionReq;
import com.ganghuitong.port.api.entity.sys.SysRole;
import com.ganghuitong.port.api.entity.sys.SysUser;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.service.sys.AccountProvisionService;
import com.ganghuitong.port.api.service.sys.LoginUserService;
import com.ganghuitong.port.api.util.PasswordUtil;
import com.ganghuitong.port.service.helper.UserRoleHelper;
import com.ganghuitong.port.service.mapper.sys.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 内置角色账号开通服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountProvisionServiceImpl implements AccountProvisionService {

    /**
     * 用户数据访问
     */
    private final SysUserMapper sysUserMapper;

    /**
     * 用户角色辅助组件
     */
    private final UserRoleHelper userRoleHelper;

    /**
     * 登录用户缓存服务
     */
    private final LoginUserService loginUserService;

    /**
     * 开通账号并赋予内置角色；账号已存在时仅补充角色并更新关联业务ID
     *
     * @param req 开通入参（账号、昵称、用户类型、关联业务ID、角色编码）
     * @return 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String provision(AccountProvisionReq req) {
        SysRole role = userRoleHelper.getRoleByCode(req.getRoleCode());
        SysUser exist = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, req.getUsername())
                .last(CommonConstant.LIMIT_ONE));
        String userId;
        if (exist != null) {
            log.info("账号[{}]已存在，补充角色[{}]", req.getUsername(), req.getRoleCode());
            userId = exist.getId();
            if (StrUtil.isBlank(exist.getRefId())) {
                SysUser update = new SysUser();
                update.setId(userId);
                update.setRefId(req.getRefId());
                sysUserMapper.updateById(update);
            }
        } else {
            log.info("为[{}]开通账号[{}]，角色[{}]", req.getNickname(), req.getUsername(), req.getRoleCode());
            String salt = PasswordUtil.randomSalt();
            SysUser user = new SysUser();
            user.setUsername(req.getUsername());
            user.setNickname(req.getNickname());
            user.setPhone(req.getUsername());
            user.setUserType(req.getUserType());
            user.setRefId(req.getRefId());
            user.setSalt(salt);
            user.setPassword(PasswordUtil.encrypt(CommonConstant.DEFAULT_PASSWORD, salt));
            user.setStatus(EnableStatusEnum.ENABLED.getCode());
            sysUserMapper.insert(user);
            userId = user.getId();
        }
        userRoleHelper.addRoleIfAbsent(userId, role.getId());
        loginUserService.evictAll();
        return userId;
    }
}
