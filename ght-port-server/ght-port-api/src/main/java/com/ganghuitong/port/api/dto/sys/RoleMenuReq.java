package com.ganghuitong.port.api.dto.sys;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 角色授权菜单入参
 *
 * @author yangxueyong
 */
@Data
public class RoleMenuReq implements Serializable {

    /**
     * 角色ID
     */
    @NotBlank(message = "角色ID不能为空")
    private String roleId;

    /**
     * 菜单ID列表
     */
    private List<String> menuIds;
}
