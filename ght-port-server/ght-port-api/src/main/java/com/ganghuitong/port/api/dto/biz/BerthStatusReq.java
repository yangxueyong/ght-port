package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 泊位状态变更入参
 *
 * @author yangxueyong
 */
@Data
public class BerthStatusReq implements Serializable {

    /**
     * 泊位ID
     */
    @NotBlank(message = "泊位ID不能为空")
    private String id;

    /**
     * 目标状态：IDLE/MAINTENANCE
     */
    @NotBlank(message = "目标状态不能为空")
    private String status;

    /**
     * 是否强制执行（泊位正在作业时需确认）
     */
    private Boolean force;
}
