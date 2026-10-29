package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 大屏驾驶舱数据
 *
 * @author yangxueyong
 */
@Data
public class CockpitVO implements Serializable {

    /**
     * 港口（含3D元素与泊位实时状态）
     */
    private PortDetailVO port;

    /**
     * 港口汇总
     */
    private CockpitSummaryVO summary;

    /**
     * 按货物类型统计
     */
    private List<CargoTypeStatVO> cargoTypes;

    /**
     * 各泊位累计统计
     */
    private List<BerthStatVO> berthStats;

    /**
     * 近7日趋势
     */
    private List<TrendVO> trend;

    /**
     * 承运商统计（按运输吨数倒序）
     */
    private List<CarrierVO> carriers;

    /**
     * 船运公司统计（按装卸吨数倒序）
     */
    private List<ShippingCompanyVO> companies;
}
