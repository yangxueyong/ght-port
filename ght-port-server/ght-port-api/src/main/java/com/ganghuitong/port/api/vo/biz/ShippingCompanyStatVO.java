package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 船运公司/船只装卸统计
 *
 * @author yangxueyong
 */
@Data
public class ShippingCompanyStatVO implements Serializable {

    /**
     * 业务ID（船运公司ID或船只ID）
     */
    private String bizId;

    /**
     * 吨数合计
     */
    private BigDecimal tonnage;

    /**
     * 方数合计
     */
    private BigDecimal volume;

    /**
     * 批次/次数
     */
    private Integer cnt;
}
