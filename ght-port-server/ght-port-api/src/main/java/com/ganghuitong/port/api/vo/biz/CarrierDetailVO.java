package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 承运商详情
 *
 * @author yangxueyong
 */
@Data
public class CarrierDetailVO implements Serializable {

    /**
     * 承运商信息
     */
    private CarrierVO carrier;

    /**
     * 车辆列表
     */
    private List<VehicleVO> vehicles;

    /**
     * 运输中的运输单
     */
    private List<TripVO> activeTrips;
}
