package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 船只保存入参
 *
 * @author yangxueyong
 */
@Data
public class ShipSaveReq implements Serializable {

    /**
     * 主键ID，为空表示新增
     */
    private String id;

    /**
     * 所属船运公司ID
     */
    @NotBlank(message = "请选择所属船运公司")
    private String companyId;

    /**
     * 船名
     */
    @NotBlank(message = "船名不能为空")
    private String shipName;

    /**
     * 船舶识别号
     */
    private String shipNo;

    /**
     * 船舶类型
     */
    @NotBlank(message = "请选择船舶类型")
    private String shipType;

    /**
     * 船长（米）
     */
    private BigDecimal shipLength;

    /**
     * 载重吨
     */
    private BigDecimal deadweight;

    /**
     * 船体颜色
     */
    private String hullColor;

    /**
     * 船长姓名
     */
    private String captainName;

    /**
     * 船长电话
     */
    private String captainPhone;

    /**
     * 备注
     */
    private String remark;
}
