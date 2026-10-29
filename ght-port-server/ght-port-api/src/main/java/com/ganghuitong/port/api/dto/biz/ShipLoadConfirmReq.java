package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 装船完成入参
 *
 * @author yangxueyong
 */
@Data
public class ShipLoadConfirmReq implements Serializable {

    /**
     * 装船分派ID
     */
    @NotBlank(message = "装船记录不能为空")
    private String id;

    /**
     * 装船吨数
     */
    @DecimalMin(value = "0", message = "装船吨数不能小于0")
    private BigDecimal loadTonnage;

    /**
     * 装船方数
     */
    @DecimalMin(value = "0", message = "装船方数不能小于0")
    private BigDecimal loadVolume;

    /**
     * 装船件数
     */
    @Min(value = 0, message = "装船件数不能小于0")
    private Integer loadPieces;
}
