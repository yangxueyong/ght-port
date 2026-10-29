package com.ganghuitong.port.service.impl.sys;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.context.UserContext;
import com.ganghuitong.port.api.dto.sys.ResetPwdReq;
import com.ganghuitong.port.api.dto.sys.StatusReq;
import com.ganghuitong.port.api.dto.sys.UserPageReq;
import com.ganghuitong.port.api.dto.sys.UserSaveReq;
import com.ganghuitong.port.api.entity.sys.SysRole;
import com.ganghuitong.port.api.entity.sys.SysUser;
import com.ganghuitong.port.api.entity.sys.SysUserRole;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.enums.UserTypeEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.sys.LoginUserService;
import com.ganghuitong.port.api.service.sys.SysUserService;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.util.PasswordUtil;
import com.ganghuitong.port.api.vo.sys.UserVO;
import com.ganghuitong.port.service.helper.UserRoleHelper;
import com.ganghuitong.port.service.mapper.sys.SysRoleMapper;
import com.ganghuitong.port.service.mapper.sys.SysUserMapper;
import com.ganghuitong.port.service.mapper.sys.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    /**
     * 用户数据访问
     */
    private final SysUserMapper sysUserMapper;

    /**
     * 用户角色关联数据访问
     */
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 角色数据访问
     */
    private final SysRoleMapper sysRoleMapper;

    /**
     * 用户角色辅助组件
     */
    private final UserRoleHelper userRoleHelper;

    /**
     * 登录用户缓存服务
     */
    private final LoginUserService loginUserService;

    /**
     * 分页查询当前租户下的用户（含角色信息）
     *
     * @param req 分页查询入参（关键字、用户类型、状态）
     * @return 用户分页数据
     */
    @Override
    public PageResp<UserVO> page(UserPageReq req) {
        LambdaQueryWrapper<SysUser> wrapper = Wrappers.<SysUser>lambdaQuery()
                .eq(StrUtil.isNotBlank(req.getUserType()), SysUser::getUserType, req.getUserType())
                .eq(StrUtil.isNotBlank(req.getStatus()), SysUser::getStatus, req.getStatus())
                .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(SysUser::getUsername, req.getKeyword())
                        .or().like(SysUser::getNickname, req.getKeyword())
                        .or().like(SysUser::getPhone, req.getKeyword()))
                .orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        List<UserVO> records = BeanUtil.copyToList(page.getRecords(), UserVO.class);
        fillRoles(records);
        return PageResp.of(page, records);
    }

    /**
     * 新增或修改用户，并重新分配角色
     *
     * @param req 用户保存入参
     * @return 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(UserSaveReq req) {
        if (!CodeEnumUtil.contains(UserTypeEnum.class, req.getUserType())) {
            throw new CustException("用户类型不正确");
        }
        SysUser exist = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, req.getUsername())
                .ne(StrUtil.isNotBlank(req.getId()), SysUser::getId, req.getId())
                .last(CommonConstant.LIMIT_ONE));
        if (exist != null) {
            throw new CustException("账号[" + req.getUsername() + "]已存在");
        }
        SysUser user = BeanUtil.copyProperties(req, SysUser.class, "password");
        if (StrUtil.isBlank(req.getId())) {
            String salt = PasswordUtil.randomSalt();
            user.setSalt(salt);
            user.setPassword(PasswordUtil.encrypt(StrUtil.blankToDefault(req.getPassword(), CommonConstant.DEFAULT_PASSWORD), salt));
            user.setStatus(EnableStatusEnum.ENABLED.getCode());
            sysUserMapper.insert(user);
            log.info("新增用户[{}]", user.getUsername());
        } else {
            sysUserMapper.updateById(user);
            log.info("修改用户[{}]", user.getUsername());
        }
        userRoleHelper.replaceRoles(user.getId(), req.getRoleIds());
        loginUserService.evictAll();
        return user.getId();
    }

    /**
     * 删除用户（不可删除自己）
     *
     * @param req 主键入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(IdReq req) {
        if (StrUtil.equals(req.getId(), UserContext.require().getUserId())) {
            throw new CustException("不能删除当前登录账号");
        }
        sysUserMapper.deleteById(req.getId());
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, req.getId()));
        loginUserService.evictAll();
    }

    /**
     * 重置用户密码
     *
     * @param req 重置密码入参（用户ID、新密码）
     */
    @Override
    public void resetPassword(ResetPwdReq req) {
        String salt = PasswordUtil.randomSalt();
        SysUser update = new SysUser();
        update.setId(req.getId());
        update.setSalt(salt);
        update.setPassword(PasswordUtil.encrypt(StrUtil.blankToDefault(req.getPassword(), CommonConstant.DEFAULT_PASSWORD), salt));
        if (sysUserMapper.updateById(update) == 0) {
            throw new CustException("用户不存在");
        }
    }

    /**
     * 启用或停用用户
     *
     * @param req 状态变更入参（用户ID、目标状态）
     */
    @Override
    public void changeStatus(StatusReq req) {
        if (!CodeEnumUtil.contains(EnableStatusEnum.class, req.getStatus())) {
            throw new CustException("状态值不正确");
        }
        if (StrUtil.equals(req.getId(), UserContext.require().getUserId())) {
            throw new CustException("不能停用当前登录账号");
        }
        SysUser update = new SysUser();
        update.setId(req.getId());
        update.setStatus(req.getStatus());
        sysUserMapper.updateById(update);
        loginUserService.evictAll();
    }

    /**
     * 为用户列表填充角色ID与角色名称
     *
     * @param users 用户列表
     */
    private void fillRoles(List<UserVO> users) {
        if (CollUtil.isEmpty(users)) {
            return;
        }
        List<String> userIds = users.stream().map(UserVO::getId).toList();
        List<SysUserRole> rels = sysUserRoleMapper.selectList(Wrappers.<SysUserRole>lambdaQuery()
                .in(SysUserRole::getUserId, userIds));
        if (CollUtil.isEmpty(rels)) {
            users.forEach(u -> {
                u.setRoleIds(Collections.emptyList());
                u.setRoleNames(Collections.emptyList());
            });
            return;
        }
        Map<String, SysRole> roleMap = sysRoleMapper.selectBatchIds(rels.stream().map(SysUserRole::getRoleId).distinct().toList())
                .stream().collect(Collectors.toMap(SysRole::getId, Function.identity()));
        Map<String, List<String>> userRoleIds = rels.stream().collect(Collectors.groupingBy(SysUserRole::getUserId,
                Collectors.mapping(SysUserRole::getRoleId, Collectors.toList())));
        for (UserVO u : users) {
            List<String> roleIds = userRoleIds.getOrDefault(u.getId(), Collections.emptyList()).stream()
                    .filter(roleMap::containsKey).toList();
            u.setRoleIds(roleIds);
            u.setRoleNames(roleIds.stream().map(id -> roleMap.get(id).getRoleName()).toList());
        }
    }
}
