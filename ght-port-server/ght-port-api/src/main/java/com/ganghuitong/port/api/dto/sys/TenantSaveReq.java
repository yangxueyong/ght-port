package com.ganghuitong.port.api.dto.sys;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户保存入参
 *
 * @author yangxueyong
 */
@Data
public class TenantSaveReq implements Serializable {

    /**
     * 主键ID，为空表示新增
     */
    private String id;

    /**
     * 租户编码
     */
    @NotBlank(message = "租户编码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{2,32}$", message = "租户编码只能包含字母、数字、下划线")
    private String tenantCode;

    /**
     * 租户名称
     */
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 到期时间
     */
    private LocalDateTime expireTime;

    /**
     * 租户管理员账号（仅新增时使用）
     */
    private String adminUsername;

    /**
     * 备注
     */
    private String remark;
}
