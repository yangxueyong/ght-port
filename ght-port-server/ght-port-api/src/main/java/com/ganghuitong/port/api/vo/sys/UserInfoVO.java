package com.ganghuitong.port.api.vo.sys;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * 当前登录用户信息
 *
 * @author yangxueyong
 */
@Data
public class UserInfoVO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 账号
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 关联业务ID
     */
    private String refId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 角色编码
     */
    private Set<String> roles;

    /**
     * 权限标识
     */
    private Set<String> perms;

    /**
     * 菜单树
     */
    private List<MenuVO> menus;
}
