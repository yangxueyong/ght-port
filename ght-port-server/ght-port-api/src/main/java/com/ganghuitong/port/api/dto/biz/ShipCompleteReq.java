package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 船只完成作业入参：卸货船只直接完成；装货船只需填写装船量
 *
 * @author yangxueyong
 */
@Data
public class ShipCompleteReq implements Serializable {

    /**
     * 船只ID
     */
    @NotBlank(message = "船只不能为空")
    private String shipId;

    /**
     * 装船吨数（装货船只必填）
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
