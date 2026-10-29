package com.ganghuitong.port.api.dto.biz;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分派承运查询入参
 *
 * @author yangxueyong
 */
@Data
public class DispatchQueryReq implements Serializable {

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 泊位到货ID
     */
    private String cargoId;

    /**
     * 状态列表
     */
    private List<String> statuses;
}
