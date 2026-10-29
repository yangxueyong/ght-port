package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.Ship;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 船只信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShipVO extends Ship {

    /**
     * 船运公司名称
     */
    private String companyName;

    /**
     * 船舶类型描述
     */
    private String shipTypeDesc;

    /**
     * 当前作业（为空表示未在作业）
     */
    private WorkingShipVO working;

    /**
     * 累计卸货吨数
     */
    private BigDecimal unloadTonnage;

    /**
     * 累计卸货方数
     */
    private BigDecimal unloadVolume;

    /**
     * 累计装货吨数
     */
    private BigDecimal loadTonnage;

    /**
     * 累计装货方数
     */
    private BigDecimal loadVolume;
}
