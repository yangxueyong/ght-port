package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 泊位累计统计
 *
 * @author yangxueyong
 */
@Data
public class BerthStatVO implements Serializable {

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 累计卸货吨数
     */
    private BigDecimal unloadTonnage;

    /**
     * 累计装货吨数
     */
    private BigDecimal loadTonnage;

    /**
     * 累计作业次数（货物批次）
     */
    private Integer workCount;

    /**
     * 累计运输车次
     */
    private Integer tripCount;

    /**
     * 按货物类型统计
     */
    private List<CargoTypeStatVO> cargoTypes;
}
