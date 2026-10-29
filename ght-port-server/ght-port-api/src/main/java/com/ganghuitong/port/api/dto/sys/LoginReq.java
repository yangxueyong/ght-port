package com.ganghuitong.port.api.dto.sys;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录入参
 *
 * @author yangxueyong
 */
@Data
public class LoginReq implements Serializable {

    /**
     * 租户编码
     */
    @NotBlank(message = "请选择所属港口（租户）")
    private String tenantCode;

    /**
     * 登录账号
     */
    @NotBlank(message = "请输入账号")
    private String username;

    /**
     * 登录密码
     */
    @NotBlank(message = "请输入密码")
    private String password;
}
