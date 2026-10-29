package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.TransportTrip;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 运输单信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TripVO extends TransportTrip {

    /**
     * 承运商名称
     */
    private String carrierName;

    /**
     * 泊位名称
     */
    private String berthName;

    /**
     * 车型
     */
    private String vehicleType;

    /**
     * 车身颜色
     */
    private String bodyColor;

    /**
     * 状态描述
     */
    private String statusDesc;
}
