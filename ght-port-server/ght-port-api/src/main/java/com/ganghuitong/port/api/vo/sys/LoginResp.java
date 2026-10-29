package com.ganghuitong.port.api.vo.sys;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录返回
 *
 * @author yangxueyong
 */
@Data
public class LoginResp implements Serializable {

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 租户名称
     */
    private String tenantName;
}
