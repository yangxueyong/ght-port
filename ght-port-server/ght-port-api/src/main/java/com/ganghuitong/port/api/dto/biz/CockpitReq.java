package com.ganghuitong.port.api.dto.biz;

import lombok.Data;

import java.io.Serializable;

/**
 * 驾驶舱查询入参
 *
 * @author yangxueyong
 */
@Data
public class CockpitReq implements Serializable {

    /**
     * 港口ID（为空时取第一个港口）
     */
    private String portId;
}
