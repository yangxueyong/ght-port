package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出门条实体（表：biz_gate_pass）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_gate_pass")
public class GatePass extends TenantEntity {

    /**
     * 出门条编号
     */
    private String passNo;

    /**
     * 访问令牌（司机链接与二维码使用，不可猜测）
     */
    private String accessToken;

    /**
     * 运输单ID
     */
    private String tripId;

    /**
     * 承运商ID
     */
    private String carrierId;

    /**
     * 承运商名称（快照）
     */
    private String carrierName;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 车牌号（快照）
     */
    private String plateNo;

    /**
     * 车型（快照）
     */
    private String vehicleType;

    /**
     * 司机姓名（快照）
     */
    private String driverName;

    /**
     * 司机电话（快照）
     */
    private String driverPhone;

    /**
     * 港口名称（快照）
     */
    private String portName;

    /**
     * 泊位名称（快照）
     */
    private String berthName;

    /**
     * 货物名称（快照）
     */
    private String cargoName;

    /**
     * 货物类型（快照）
     */
    private String cargoTypeName;

    /**
     * 装载吨数（快照）
     */
    private BigDecimal loadTonnage;

    /**
     * 装载方数（快照）
     */
    private BigDecimal loadVolume;

    /**
     * 装载件数（快照）
     */
    private Integer loadPieces;

    /**
     * 开具时间
     */
    private LocalDateTime issueTime;

    /**
     * 状态：UNUSED-未核销，VERIFIED-已核销
     */
    private String status;

    /**
     * 核销时间
     */
    private LocalDateTime verifyTime;

    /**
     * 核销人
     */
    private String verifyBy;
}
