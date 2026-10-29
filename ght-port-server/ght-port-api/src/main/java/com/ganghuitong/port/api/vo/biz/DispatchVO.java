package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.Dispatch;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 分派承运信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DispatchVO extends Dispatch {

    /**
     * 承运商名称
     */
    private String carrierName;

    /**
     * 货物名称
     */
    private String cargoName;

    /**
     * 车牌号
     */
    private String plateNo;

    /**
     * 车型
     */
    private String vehicleType;

    /**
     * 车长
     */
    private BigDecimal vehicleLength;

    /**
     * 核定载重
     */
    private BigDecimal loadCapacity;

    /**
     * 车身颜色
     */
    private String bodyColor;

    /**
     * 司机姓名
     */
    private String driverName;

    /**
     * 司机电话
     */
    private String driverPhone;

    /**
     * 车辆当前运输状态
     */
    private String vehicleTransportStatus;

    /**
     * 出发后生成的运输单
     */
    private TripVO trip;

    /**
     * 出门条令牌
     */
    private String passToken;

    /**
     * 出门条状态
     */
    private String passStatus;
}
