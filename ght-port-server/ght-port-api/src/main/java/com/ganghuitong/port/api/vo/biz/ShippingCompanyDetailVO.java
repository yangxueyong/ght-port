package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 船运公司详情：基本信息与统计、船只（含作业状态）
 *
 * @author yangxueyong
 */
@Data
public class ShippingCompanyDetailVO implements Serializable {

    /**
     * 船运公司信息（含统计）
     */
    private ShippingCompanyVO company;

    /**
     * 船只列表（含作业状态与统计）
     */
    private List<ShipVO> ships;
}
