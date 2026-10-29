package com.ganghuitong.port.api.dto.sys;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 启停状态变更入参
 *
 * @author yangxueyong
 */
@Data
public class StatusReq implements Serializable {

    /**
     * 主键ID
     */
    @NotBlank(message = "ID不能为空")
    private String id;

    /**
     * 目标状态：ENABLED/DISABLED
     */
    @NotBlank(message = "状态不能为空")
    private String status;
}
