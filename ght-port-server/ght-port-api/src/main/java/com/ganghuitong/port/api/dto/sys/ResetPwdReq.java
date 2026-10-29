package com.ganghuitong.port.api.dto.sys;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 重置密码入参
 *
 * @author yangxueyong
 */
@Data
public class ResetPwdReq implements Serializable {

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String id;

    /**
     * 新密码，为空则重置为默认密码
     */
    private String password;
}
