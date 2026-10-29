package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 港口俯视图元素入参
 *
 * @author yangxueyong
 */
@Data
public class PortElementReq implements Serializable {

    /**
     * 元素类型
     */
    @NotBlank(message = "元素类型不能为空")
    private String elementType;

    /**
     * 左上角X坐标
     */
    @NotNull(message = "X坐标不能为空")
    private BigDecimal posX;

    /**
     * 左上角Y坐标
     */
    @NotNull(message = "Y坐标不能为空")
    private BigDecimal posY;

    /**
     * 宽度
     */
    @NotNull(message = "宽度不能为空")
    private BigDecimal width;

    /**
     * 高度
     */
    @NotNull(message = "高度不能为空")
    private BigDecimal height;

    /**
     * 旋转角度
     */
    private BigDecimal rotation;

    /**
     * 图层顺序
     */
    private Integer layerIndex;

    /**
     * 外观属性JSON文本
     */
    private String props;

    /**
     * 已存在的泊位ID（泊位元素，新画的泊位为空）
     */
    private String berthId;

    /**
     * 泊位编号（泊位元素）
     */
    private String berthCode;

    /**
     * 泊位名称（泊位元素）
     */
    private String berthName;
}
