package com.ganghuitong.port.api.context;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 当前登录用户信息
 *
 * @author yangxueyong
 */
@Data
public class LoginUser implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户类型：ADMIN/STAFF/CARRIER/DRIVER/GUARD
     */
    private String userType;

    /**
     * 关联业务ID（承运商账号关联承运商ID，司机账号关联车辆ID）
     */
    private String refId;

    /**
     * 角色编码集合
     */
    private Set<String> roleCodes;

    /**
     * 权限标识集合
     */
    private Set<String> perms;
}
