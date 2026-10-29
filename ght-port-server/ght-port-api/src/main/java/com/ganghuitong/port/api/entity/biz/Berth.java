package com.ganghuitong.port.api.entity.biz;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 泊位实体（表：biz_berth）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_berth")
public class Berth extends TenantEntity {

    /**
     * 港口ID
     */
    private String portId;

    /**
     * 泊位编号
     */
    private String berthCode;

    /**
     * 泊位名称
     */
    private String berthName;

    /**
     * 泊位状态：IDLE-未作业，WORKING-正在作业，MAINTENANCE-维修中
     */
    private String status;

    /**
     * 每日作业开始时间
     */
    @JSONField(format = "HH:mm:ss")
    private LocalTime workStartTime;

    /**
     * 每日作业结束时间
     */
    @JSONField(format = "HH:mm:ss")
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
     * 最大靠泊吨级（吨）
     */
    private BigDecimal maxTonnage;

    /**
     * 备注
     */
    private String remark;
}
