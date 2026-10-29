package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.Carrier;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 承运商信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CarrierVO extends Carrier {

    /**
     * 车辆数
     */
    private Integer vehicleCount;

    /**
     * 运输中车辆数
     */
    private Integer inTransitVehicleCount;

    /**
     * 运输统计
     */
    private CarrierStatVO stat;
}
