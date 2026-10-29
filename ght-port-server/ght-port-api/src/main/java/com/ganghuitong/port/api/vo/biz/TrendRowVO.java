package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 按日汇总行
 *
 * @author yangxueyong
 */
@Data
public class TrendRowVO implements Serializable {

    /**
     * 日期（yyyy-MM-dd）
     */
    private String day;

    /**
     * 吨数合计
     */
    private BigDecimal tonnage;
}
