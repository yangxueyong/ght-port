package com.ganghuitong.port.api.dto.biz;

import com.ganghuitong.port.api.common.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 装船记录分页查询入参
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShipLoadPageReq extends PageReq {

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 泊位货物ID
     */
    private String cargoId;

    /**
     * 船运公司ID
     */
    private String companyId;

    /**
     * 状态列表
     */
    private List<String> statuses;
}
