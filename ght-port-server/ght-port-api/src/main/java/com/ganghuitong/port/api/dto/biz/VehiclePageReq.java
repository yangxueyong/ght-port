package com.ganghuitong.port.api.dto.biz;

import com.ganghuitong.port.api.common.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆分页查询入参
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehiclePageReq extends PageReq {

    /**
     * 承运商ID
     */
    private String carrierId;

    /**
     * 运输状态
     */
    private String transportStatus;

    /**
     * 车型
     */
    private String vehicleType;
}
