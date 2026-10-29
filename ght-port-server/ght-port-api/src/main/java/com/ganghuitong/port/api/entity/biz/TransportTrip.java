package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运输单实体（表：biz_transport_trip）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_transport_trip")
public class TransportTrip extends TenantEntity {

    /**
     * 运输单号
     */
    private String tripNo;

    /**
     * 分派承运ID
     */
    private String dispatchId;

    /**
     * 港口ID
     */
    private String portId;

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 泊位到货ID
     */
    private String cargoId;

    /**
     * 承运商ID
     */
    private String carrierId;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 车牌号（冗余）
     */
    private String plateNo;

    /**
     * 司机姓名（冗余）
     */
    private String driverName;

    /**
     * 司机电话（冗余）
     */
    private String driverPhone;

    /**
     * 货物名称（冗余）
     */
    private String cargoName;

    /**
     * 货物类型名称（冗余）
     */
    private String cargoTypeName;

    /**
     * 装载吨数
     */
    private BigDecimal loadTonnage;

    /**
     * 装载方数
     */
    private BigDecimal loadVolume;

    /**
     * 装载件数
     */
    private Integer loadPieces;

    /**
     * 状态：IN_TRANSIT-运输中，COMPLETED-已完成
     */
    private String status;

    /**
     * 出发时间
     */
    private LocalDateTime departTime;

    /**
     * 完成运输时间
     */
    private LocalDateTime completeTime;

    /**
     * 设置完成运输的操作人
     */
    private String completeBy;
}
