package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 首页概览
 *
 * @author yangxueyong
 */
@Data
public class DashboardVO implements Serializable {

    /**
     * 港口数
     */
    private Integer portCount;

    /**
     * 未作业泊位数
     */
    private Integer berthIdle;

    /**
     * 正在作业泊位数
     */
    private Integer berthWorking;

    /**
     * 维修中泊位数
     */
    private Integer berthMaintenance;

    /**
     * 承运商数
     */
    private Integer carrierCount;

    /**
     * 车辆数
     */
    private Integer vehicleCount;

    /**
     * 运输中车辆数
     */
    private Integer vehicleInTransit;

    /**
     * 船运公司数
     */
    private Integer companyCount;

    /**
     * 今日出发车次
     */
    private Integer todayTrips;

    /**
     * 今日出港吨数
     */
    private BigDecimal todayTonnage;

    /**
     * 未核销出门条数
     */
    private Integer unverifiedPasses;

    /**
     * 最近运输动态
     */
    private List<TripVO> recentTrips;
}
