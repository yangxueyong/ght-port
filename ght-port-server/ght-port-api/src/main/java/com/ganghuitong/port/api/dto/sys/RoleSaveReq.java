package com.ganghuitong.port.api.dto.sys;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色保存入参
 *
 * @author yangxueyong
 */
@Data
public class RoleSaveReq implements Serializable {

    /**
     * 主键ID，为空表示新增
     */
    private String id;

    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
