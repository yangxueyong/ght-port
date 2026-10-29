package com.ganghuitong.port.api.dto.biz;

import com.alibaba.fastjson2.annotation.JSONField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 泊位作业时间设置入参
 *
 * @author yangxueyong
 */
@Data
public class BerthWorkTimeReq implements Serializable {

    /**
     * 泊位ID
     */
    @NotBlank(message = "泊位ID不能为空")
    private String id;

    /**
     * 作业开始时间
     */
    @JSONField(format = "HH:mm:ss")
    @NotNull(message = "请选择作业开始时间")
    private LocalTime workStartTime;

    /**
     * 作业结束时间
     */
    @JSONField(format = "HH:mm:ss")
    @NotNull(message = "请选择作业结束时间")
    private LocalTime workEndTime;

    /**
     * 泊位长度（米）
     */
    private BigDecimal berthLength;

    /**
     * 前沿水深（米）
     */
    private BigDecimal waterDepth;

    /**
     * 最大靠泊吨级
     */
    private BigDecimal maxTonnage;

    /**
     * 是否强制执行（泊位正在作业时需确认）
     */
    private Boolean force;
}
