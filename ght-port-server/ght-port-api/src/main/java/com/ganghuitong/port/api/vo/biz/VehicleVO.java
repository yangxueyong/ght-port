package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.Vehicle;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleVO extends Vehicle {

    /**
     * 承运商名称
     */
    private String carrierName;

    /**
     * 车型描述
     */
    private String vehicleTypeDesc;

    /**
     * 当前运输中的运输单
     */
    private TripVO currentTrip;
}
