package com.ganghuitong.port.api.entity.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单权限实体（表：sys_menu）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    /**
     * 父菜单ID，顶级为0
     */
    private String parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型：DIR-目录，MENU-菜单，BUTTON-按钮
     */
    private String menuType;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 前端组件路径
     */
    private String component;

    /**
     * 权限标识
     */
    private String perms;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否显示：1-显示，0-隐藏
     */
    private Integer visible;

    /**
     * 状态：ENABLED-启用，DISABLED-停用
     */
    private String status;
}
