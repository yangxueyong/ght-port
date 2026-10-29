package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 按货物类型统计
 *
 * @author yangxueyong
 */
@Data
public class CargoTypeStatVO implements Serializable {

    /**
     * 货物类型名称
     */
    private String typeName;

    /**
     * 展示颜色
     */
    private String color;

    /**
     * 卸货吨数
     */
    private BigDecimal unloadTonnage;

    /**
     * 装货吨数
     */
    private BigDecimal loadTonnage;

    /**
     * 作业批次
     */
    private Integer count;
}
