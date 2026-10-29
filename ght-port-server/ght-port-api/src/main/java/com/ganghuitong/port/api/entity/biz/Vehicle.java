package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 货车实体（表：biz_vehicle）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_vehicle")
public class Vehicle extends TenantEntity {

    /**
     * 所属承运商ID
     */
    private String carrierId;

    /**
     * 车牌号
     */
    private String plateNo;

    /**
     * 车型：LIGHT_VAN/MEDIUM_BOX/HEAVY_CARGO/SEMI_TRAILER/DUMP/TANKER/FLATBED/CONTAINER
     */
    private String vehicleType;

    /**
     * 车长（米）
     */
    private BigDecimal vehicleLength;

    /**
     * 核定载重（吨）
     */
    private BigDecimal loadCapacity;

    /**
     * 车身颜色
     */
    private String bodyColor;

    /**
     * 司机姓名
     */
    private String driverName;

    /**
     * 司机电话（同时作为司机登录账号）
     */
    private String driverPhone;

    /**
     * 司机登录账号用户ID
     */
    private String driverUserId;

    /**
     * 运输状态：IDLE-空闲，IN_TRANSIT-运输中
     */
    private String transportStatus;

    /**
     * 状态：ENABLED-启用，DISABLED-停用
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
