package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 驾驶舱运输统计行（按泊位分组）
 *
 * @author yangxueyong
 */
@Data
public class CockpitTripRowVO implements Serializable {

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 运输车次
     */
    private Integer tripCount;

    /**
     * 运输中车次
     */
    private Integer inTransitCount;

    /**
     * 运输吨数合计
     */
    private BigDecimal tonnage;
}
