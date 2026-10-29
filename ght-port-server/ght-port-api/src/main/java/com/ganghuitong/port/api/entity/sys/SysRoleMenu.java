package com.ganghuitong.port.api.entity.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单关联实体（表：sys_role_menu）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role_menu")
public class SysRoleMenu extends TenantEntity {

    /**
     * 角色ID
     */
    private String roleId;

    /**
     * 菜单ID
     */
    private String menuId;
}
