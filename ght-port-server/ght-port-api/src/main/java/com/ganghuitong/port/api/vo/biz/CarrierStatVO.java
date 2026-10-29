package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 承运商运输统计
 *
 * @author yangxueyong
 */
@Data
public class CarrierStatVO implements Serializable {

    /**
     * 承运商ID
     */
    private String carrierId;

    /**
     * 运输趟数
     */
    private Integer tripCount;

    /**
     * 运输中趟数
     */
    private Integer inTransitCount;

    /**
     * 累计运输吨数
     */
    private BigDecimal totalTonnage;

    /**
     * 累计运输方数
     */
    private BigDecimal totalVolume;

    /**
     * 累计运输件数
     */
    private Integer totalPieces;
}
