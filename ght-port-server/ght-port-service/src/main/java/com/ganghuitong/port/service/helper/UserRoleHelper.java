package com.ganghuitong.port.service.helper;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.entity.sys.SysRole;
import com.ganghuitong.port.api.entity.sys.SysUserRole;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.service.mapper.sys.SysRoleMapper;
import com.ganghuitong.port.service.mapper.sys.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户角色关联辅助组件（用户管理与账号开通复用）
 *
 * @author yangxueyong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserRoleHelper {

    /**
     * 用户角色关联数据访问
     */
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 角色数据访问
     */
    private final SysRoleMapper sysRoleMapper;

    /**
     * 覆盖式设置用户角色（先删后增），调用方需开启事务
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表，为空表示清空角色
     */
    public void replaceRoles(String userId, List<String> roleIds) {
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getUserId, userId));
        if (CollUtil.isEmpty(roleIds)) {
            log.info("用户[{}]角色已清空", userId);
            return;
        }
        for (String roleId : CollUtil.distinct(roleIds)) {
            SysUserRole rel = new SysUserRole();
            rel.setUserId(userId);
            rel.setRoleId(roleId);
            sysUserRoleMapper.insert(rel);
        }
    }

    /**
     * 为用户追加角色，已拥有时忽略
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     */
    public void addRoleIfAbsent(String userId, String roleId) {
        Long count = sysUserRoleMapper.selectCount(Wrappers.<SysUserRole>lambdaQuery()
                .eq(SysUserRole::getUserId, userId)
                .eq(SysUserRole::getRoleId, roleId));
        if (count > 0) {
            log.info("用户[{}]已拥有角色[{}]，无需追加", userId, roleId);
            return;
        }
        SysUserRole rel = new SysUserRole();
        rel.setUserId(userId);
        rel.setRoleId(roleId);
        sysUserRoleMapper.insert(rel);
    }

    /**
     * 根据角色编码获取当前租户的角色，不存在时抛出异常
     *
     * @param roleCode 角色编码
     * @return 角色实体
     */
    public SysRole getRoleByCode(String roleCode) {
        SysRole role = sysRoleMapper.selectOne(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getRoleCode, roleCode)
                .last(CommonConstant.LIMIT_ONE));
        if (role == null) {
            log.warn("当前租户缺少内置角色[{}]", roleCode);
            throw new CustException("系统缺少内置角色：" + roleCode + "，请联系管理员初始化");
        }
        return role;
    }
}
