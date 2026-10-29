package com.ganghuitong.port.api.dto.biz;

import com.ganghuitong.port.api.common.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出门条分页查询入参
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GatePassPageReq extends PageReq {

    /**
     * 核销状态
     */
    private String status;

    /**
     * 承运商ID
     */
    private String carrierId;
}
