package com.ganghuitong.port.api.dto.biz;

import com.ganghuitong.port.api.common.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运输单分页查询入参
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TripPageReq extends PageReq {

    /**
     * 承运商ID
     */
    private String carrierId;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 运输状态
     */
    private String status;
}
