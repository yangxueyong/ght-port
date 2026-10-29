package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 按日趋势
 *
 * @author yangxueyong
 */
@Data
public class TrendVO implements Serializable {

    /**
     * 日期（yyyy-MM-dd）
     */
    private String day;

    /**
     * 卸货吨数
     */
    private BigDecimal unloadTonnage;

    /**
     * 装货吨数
     */
    private BigDecimal loadTonnage;

    /**
     * 车辆运出吨数
     */
    private BigDecimal tripTonnage;
}
