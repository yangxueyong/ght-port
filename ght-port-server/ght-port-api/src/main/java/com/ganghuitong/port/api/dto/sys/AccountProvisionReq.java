package com.ganghuitong.port.api.dto.sys;

import lombok.Data;

import java.io.Serializable;

/**
 * 内置角色账号开通入参
 *
 * @author yangxueyong
 */
@Data
public class AccountProvisionReq implements Serializable {

    /**
     * 登录账号（一般为手机号）
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
     * 需要赋予的内置角色编码
     */
    private String roleCode;
}
