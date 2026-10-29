package com.ganghuitong.port.api.entity.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体（表：sys_role）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends TenantEntity {

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 是否内置角色：1-是，0-否（内置角色不可删除）
     */
    private Integer builtin;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：ENABLED-启用，DISABLED-停用
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
