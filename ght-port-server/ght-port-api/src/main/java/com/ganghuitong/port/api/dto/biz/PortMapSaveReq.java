package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 港口俯视图保存入参
 *
 * @author yangxueyong
 */
@Data
public class PortMapSaveReq implements Serializable {

    /**
     * 港口ID
     */
    @NotBlank(message = "港口ID不能为空")
    private String portId;

    /**
     * 画布宽度
     */
    private Integer canvasWidth;

    /**
     * 画布高度
     */
    private Integer canvasHeight;

    /**
     * 主题
     */
    private String theme;

    /**
     * 是否强制保存（涉及正在作业泊位时需用户确认）
     */
    private Boolean force;

    /**
     * 元素列表
     */
    @Valid
    private List<PortElementReq> elements;
}
