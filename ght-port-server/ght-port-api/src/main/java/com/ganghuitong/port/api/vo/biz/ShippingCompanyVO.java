package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.ShippingCompany;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 船运公司信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShippingCompanyVO extends ShippingCompany {

    /**
     * 船只数量
     */
    private Integer shipCount;

    /**
     * 正在作业的船只数量
     */
    private Integer workingShipCount;

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

    /**
     * 船只列表
     */
    private List<ShipVO> ships;
}
