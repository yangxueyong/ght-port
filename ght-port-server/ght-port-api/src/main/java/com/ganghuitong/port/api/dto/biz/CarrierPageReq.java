package com.ganghuitong.port.api.dto.biz;

import com.ganghuitong.port.api.common.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 承运商分页查询入参
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CarrierPageReq extends PageReq {

    /**
     * 状态
     */
    private String status;

    /**
     * 仅查看有车辆运输中的承运商
     */
    private Boolean onlyInTransit;
}
