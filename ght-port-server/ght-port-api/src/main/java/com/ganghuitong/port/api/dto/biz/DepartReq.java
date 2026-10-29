package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 装载完成立即出发入参
 *
 * @author yangxueyong
 */
@Data
public class DepartReq implements Serializable {

    /**
     * 分派承运ID
     */
    @NotBlank(message = "分派记录不能为空")
    private String dispatchId;

    /**
     * 装载吨数
     */
    @DecimalMin(value = "0", message = "装载吨数不能小于0")
    private BigDecimal loadTonnage;

    /**
     * 装载方数
     */
    @DecimalMin(value = "0", message = "装载方数不能小于0")
    private BigDecimal loadVolume;

    /**
     * 装载件数
     */
    @Min(value = 0, message = "装载件数不能小于0")
    private Integer loadPieces;
}
