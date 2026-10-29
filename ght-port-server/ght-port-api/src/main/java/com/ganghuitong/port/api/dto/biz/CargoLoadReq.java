package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 泊位装货入参
 *
 * @author yangxueyong
 */
@Data
public class CargoLoadReq implements Serializable {

    /**
     * 泊位ID
     */
    @NotBlank(message = "泊位ID不能为空")
    private String berthId;

    /**
     * 货物类型ID
     */
    @NotBlank(message = "请选择货物类型")
    private String cargoTypeId;

    /**
     * 货物名称
     */
    @NotBlank(message = "请输入货物名称")
    private String cargoName;

    /**
     * 货物形态：CONTAINER-集装箱，BULK-散货
     */
    @NotBlank(message = "请选择货物形态")
    private String packType;

    /**
     * 货物吨位
     */
    @NotNull(message = "请输入货物吨位")
    @DecimalMin(value = "0", message = "货物吨位不能小于0")
    private BigDecimal tonnage;

    /**
     * 货物方数
     */
    @DecimalMin(value = "0", message = "货物方数不能小于0")
    private BigDecimal volume;

    /**
     * 货物件数
     */
    @Min(value = 0, message = "货物件数不能小于0")
    private Integer pieces;

    /**
     * 备注
     */
    private String remark;
}
