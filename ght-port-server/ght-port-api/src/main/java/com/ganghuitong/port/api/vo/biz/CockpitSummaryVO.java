package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 驾驶舱港口汇总
 *
 * @author yangxueyong
 */
@Data
public class CockpitSummaryVO implements Serializable {

    /**
     * 累计卸货吨数
     */
    private BigDecimal unloadTonnage;

    /**
     * 累计装货吨数
     */
    private BigDecimal loadTonnage;

    /**
     * 累计卸货方数
     */
    private BigDecimal unloadVolume;

    /**
     * 累计装货方数
     */
    private BigDecimal loadVolume;

    /**
     * 累计作业次数（到货+装货批次）
     */
    private Integer workCount;

    /**
     * 到货批次
     */
    private Integer unloadCount;

    /**
     * 装货批次
     */
    private Integer loadCount;

    /**
     * 累计运输车次
     */
    private Integer tripCount;

    /**
     * 累计车辆运出吨数
     */
    private BigDecimal tripTonnage;

    /**
     * 运输中车辆
     */
    private Integer inTransitCount;

    /**
     * 承运商数量
     */
    private Integer carrierCount;

    /**
     * 车辆数量
     */
    private Integer vehicleCount;

    /**
     * 船运公司数量
     */
    private Integer companyCount;

    /**
     * 船只数量
     */
    private Integer shipCount;

    /**
     * 正在作业的船只
     */
    private Integer workingShipCount;
}
