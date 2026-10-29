package com.ganghuitong.port.api.dto.sys;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜单保存入参
 *
 * @author yangxueyong
 */
@Data
public class MenuSaveReq implements Serializable {

    /**
     * 主键ID，为空表示新增
     */
    private String id;

    /**
     * 父菜单ID
     */
    private String parentId;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    /**
     * 菜单类型：DIR/MENU/BUTTON
     */
    @NotBlank(message = "菜单类型不能为空")
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
     * 是否显示
     */
    private Integer visible;
}
