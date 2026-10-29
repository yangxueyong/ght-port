package com.ganghuitong.port.api.dto.sys;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改密码入参
 *
 * @author yangxueyong
 */
@Data
public class ChangePwdReq implements Serializable {

    /**
     * 原密码
     */
    @NotBlank(message = "请输入原密码")
    private String oldPassword;

    /**
     * 新密码
     */
    @NotBlank(message = "请输入新密码")
    @Size(min = 6, max = 32, message = "新密码长度为6-32位")
    private String newPassword;
}
