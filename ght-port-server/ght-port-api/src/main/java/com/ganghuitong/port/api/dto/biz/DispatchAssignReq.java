package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分派承运入参
 *
 * @author yangxueyong
 */
@Data
public class DispatchAssignReq implements Serializable {

    /**
     * 泊位ID
     */
    @NotBlank(message = "泊位ID不能为空")
    private String berthId;

    /**
     * 泊位到货ID
     */
    @NotBlank(message = "请选择要承运的货物")
    private String cargoId;

    /**
     * 承运商ID
     */
    @NotBlank(message = "请选择承运商")
    private String carrierId;

    /**
     * 车辆ID列表
     */
    @NotEmpty(message = "请选择货车")
    private List<String> vehicleIds;
}
