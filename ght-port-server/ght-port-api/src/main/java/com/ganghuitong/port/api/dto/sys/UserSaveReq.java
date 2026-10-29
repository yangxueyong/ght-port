package com.ganghuitong.port.api.dto.sys;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户保存入参
 *
 * @author yangxueyong
 */
@Data
public class UserSaveReq implements Serializable {

    /**
     * 主键ID，为空表示新增
     */
    private String id;

    /**
     * 登录账号
     */
    @NotBlank(message = "账号不能为空")
    private String username;

    /**
     * 昵称
     */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户类型
     */
    @NotBlank(message = "用户类型不能为空")
    private String userType;

    /**
     * 初始密码（新增时为空则使用默认密码）
     */
    private String password;

    /**
     * 角色ID列表
     */
    private List<String> roleIds;

    /**
     * 备注
     */
    private String remark;
}
