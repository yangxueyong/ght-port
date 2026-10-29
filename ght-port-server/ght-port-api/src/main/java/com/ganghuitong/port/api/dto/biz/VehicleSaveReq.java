package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 车辆保存入参
 *
 * @author yangxueyong
 */
@Data
public class VehicleSaveReq implements Serializable {

    /**
     * 主键ID，为空表示新增
     */
    private String id;

    /**
     * 所属承运商ID（承运商账号操作时自动取本承运商）
     */
    private String carrierId;

    /**
     * 车牌号
     */
    @NotBlank(message = "车牌号不能为空")
    private String plateNo;

    /**
     * 车型
     */
    @NotBlank(message = "请选择车型")
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
    @NotBlank(message = "司机姓名不能为空")
    private String driverName;

    /**
     * 司机电话（作为司机登录账号）
     */
    @NotBlank(message = "司机电话不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "请输入正确的手机号")
    private String driverPhone;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 是否强制保存（车辆运输中时需确认）
     */
    private Boolean force;
}
