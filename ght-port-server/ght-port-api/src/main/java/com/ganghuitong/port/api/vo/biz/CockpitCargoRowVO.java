package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 驾驶舱货物统计行（按泊位、货物类型、作业方向分组）
 *
 * @author yangxueyong
 */
@Data
public class CockpitCargoRowVO implements Serializable {

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 货物类型名称
     */
    private String typeName;

    /**
     * 货物类型颜色
     */
    private String color;

    /**
     * 作业方向：UNLOAD-到货，LOAD-装货
     */
    private String direction;

    /**
     * 货物吨位合计
     */
    private BigDecimal tonnage;

    /**
     * 已装载吨位合计
     */
    private BigDecimal loadedTonnage;

    /**
     * 货物方数合计
     */
    private BigDecimal volume;

    /**
     * 已装载方数合计
     */
    private BigDecimal loadedVolume;

    /**
     * 批次数
     */
    private Integer cnt;
}
