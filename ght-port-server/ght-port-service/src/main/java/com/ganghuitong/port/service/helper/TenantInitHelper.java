package com.ganghuitong.port.service.helper;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.entity.sys.SysRole;
import com.ganghuitong.port.api.entity.sys.SysRoleMenu;
import com.ganghuitong.port.api.entity.sys.SysTenant;
import com.ganghuitong.port.api.entity.sys.SysUser;
import com.ganghuitong.port.api.entity.sys.SysUserRole;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.enums.RoleCodeEnum;
import com.ganghuitong.port.api.enums.UserTypeEnum;
import com.ganghuitong.port.api.util.PasswordUtil;
import com.ganghuitong.port.service.mapper.sys.SysRoleMapper;
import com.ganghuitong.port.service.mapper.sys.SysRoleMenuMapper;
import com.ganghuitong.port.service.mapper.sys.SysUserMapper;
import com.ganghuitong.port.service.mapper.sys.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 新租户初始化辅助组件：从平台租户复制内置角色及其菜单权限，并创建租户管理员
 *
 * @author yangxueyong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInitHelper {

    /**
     * 角色数据访问
     */
    private final SysRoleMapper sysRoleMapper;

    /**
     * 角色菜单关联数据访问
     */
    private final SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 用户数据访问
     */
    private final SysUserMapper sysUserMapper;

    /**
     * 用户角色关联数据访问
     */
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 初始化新租户（需在平台租户上下文、事务中调用）
     *
     * @param tenant        新租户
     * @param adminUsername 租户管理员账号
     */
    public void init(SysTenant tenant, String adminUsername) {
        List<SysRole> templates = sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getBuiltin, CommonConstant.YES)
                .ne(SysRole::getRoleCode, RoleCodeEnum.SUPER_ADMIN.getCode()));
        String adminRoleId = null;
        for (SysRole template : templates) {
            String newRoleId = copyRole(template, tenant.getId());
            if (StrUtil.equals(RoleCodeEnum.TENANT_ADMIN.getCode(), template.getRoleCode())) {
                adminRoleId = newRoleId;
            }
        }
        log.info("租户[{}]已复制内置角色{}个", tenant.getTenantCode(), templates.size());
        String salt = PasswordUtil.randomSalt();
        SysUser admin = new SysUser();
        admin.setTenantId(tenant.getId());
        admin.setUsername(adminUsername);
        admin.setNickname(tenant.getTenantName() + "管理员");
        admin.setPhone(tenant.getContactPhone());
        admin.setUserType(UserTypeEnum.ADMIN.getCode());
        admin.setSalt(salt);
        admin.setPassword(PasswordUtil.encrypt(CommonConstant.DEFAULT_PASSWORD, salt));
        admin.setStatus(EnableStatusEnum.ENABLED.getCode());
        sysUserMapper.insert(admin);
        if (adminRoleId != null) {
            SysUserRole rel = new SysUserRole();
            rel.setTenantId(tenant.getId());
            rel.setUserId(admin.getId());
            rel.setRoleId(adminRoleId);
            sysUserRoleMapper.insert(rel);
        } else {
            log.warn("平台租户缺少租户管理员角色模板，管理员[{}]未分配角色", adminUsername);
        }
    }

    /**
     * 复制单个角色及其菜单权限到新租户
     *
     * @param template 模板角色
     * @param tenantId 新租户ID
     * @return 新角色ID
     */
    private String copyRole(SysRole template, String tenantId) {
        SysRole role = new SysRole();
        role.setTenantId(tenantId);
        role.setRoleCode(template.getRoleCode());
        role.setRoleName(template.getRoleName());
        role.setBuiltin(CommonConstant.YES);
        role.setSort(template.getSort());
        role.setStatus(EnableStatusEnum.ENABLED.getCode());
        role.setRemark(template.getRemark());
        sysRoleMapper.insert(role);
        List<SysRoleMenu> menus = sysRoleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery()
                .eq(SysRoleMenu::getRoleId, template.getId()));
        for (SysRoleMenu m : menus) {
            SysRoleMenu rel = new SysRoleMenu();
            rel.setTenantId(tenantId);
            rel.setRoleId(role.getId());
            rel.setMenuId(m.getMenuId());
            sysRoleMenuMapper.insert(rel);
        }
        return role.getId();
    }
}
