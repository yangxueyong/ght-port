package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分派承运实体（表：biz_dispatch）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_dispatch")
public class Dispatch extends TenantEntity {

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 泊位到货ID
     */
    private String cargoId;

    /**
     * 承运商ID
     */
    private String carrierId;

    /**
     * 车辆ID
     */
    private String vehicleId;

    /**
     * 状态：WAITING-待装载，DEPARTED-已出发，CANCELLED-已取消
     */
    private String status;

    /**
     * 出发后生成的运输单ID
     */
    private String tripId;
}
