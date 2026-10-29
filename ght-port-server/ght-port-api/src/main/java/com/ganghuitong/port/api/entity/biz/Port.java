package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 港口实体（表：biz_port）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_port")
public class Port extends TenantEntity {

    /**
     * 港口名称
     */
    private String portName;

    /**
     * 港口编码
     */
    private String portCode;

    /**
     * 港口地址
     */
    private String address;

    /**
     * 俯视图画布宽度（像素）
     */
    private Integer canvasWidth;

    /**
     * 俯视图画布高度（像素）
     */
    private Integer canvasHeight;

    /**
     * 俯视图主题：DAY-白昼，DUSK-黄昏，NIGHT-夜间
     */
    private String theme;

    /**
     * 港口简介
     */
    private String description;
}
