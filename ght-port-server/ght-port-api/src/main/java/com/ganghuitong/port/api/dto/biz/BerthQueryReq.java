package com.ganghuitong.port.api.dto.biz;

import lombok.Data;

import java.io.Serializable;

/**
 * 泊位查询入参
 *
 * @author yangxueyong
 */
@Data
public class BerthQueryReq implements Serializable {

    /**
     * 港口ID
     */
    private String portId;

    /**
     * 泊位状态
     */
    private String status;
}
