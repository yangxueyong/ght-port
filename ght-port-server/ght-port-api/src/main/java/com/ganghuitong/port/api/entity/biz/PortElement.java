package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 港口俯视图元素实体（表：biz_port_element）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_port_element")
public class PortElement extends TenantEntity {

    /**
     * 港口ID
     */
    private String portId;

    /**
     * 元素类型：RIVER/LAND/BERTH/ROAD/YARD/WAREHOUSE/CRANE/GATE/BUILDING/TANK/TREE/SHIP/LABEL
     */
    private String elementType;

    /**
     * 左上角X坐标
     */
    private BigDecimal posX;

    /**
     * 左上角Y坐标
     */
    private BigDecimal posY;

    /**
     * 宽度
     */
    private BigDecimal width;

    /**
     * 高度
     */
    private BigDecimal height;

    /**
     * 旋转角度（度）
     */
    private BigDecimal rotation;

    /**
     * 图层顺序，越大越靠上
     */
    private Integer layerIndex;

    /**
     * 外观属性（JSON文本，由前端解释，如颜色、文字、变体）
     */
    private String props;

    /**
     * 关联泊位ID（仅泊位元素有值）
     */
    private String berthId;
}
