package com.ganghuitong.port.api.dto.biz;

import lombok.Data;

import java.io.Serializable;

/**
 * 设置运输完成入参
 *
 * @author yangxueyong
 */
@Data
public class TripCompleteReq implements Serializable {

    /**
     * 运输单ID（与车辆ID二选一）
     */
    private String tripId;

    /**
     * 车辆ID（按车辆完成其运输中的运输单）
     */
    private String vehicleId;
}
