package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 装货分派船只入参
 *
 * @author yangxueyong
 */
@Data
public class ShipLoadAssignReq implements Serializable {

    /**
     * 泊位ID
     */
    @NotBlank(message = "泊位ID不能为空")
    private String berthId;

    /**
     * 泊位货物ID（装货）
     */
    @NotBlank(message = "请选择装船货物")
    private String cargoId;

    /**
     * 船只ID列表
     */
    @NotEmpty(message = "请选择船只")
    private List<String> shipIds;
}
