package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 货物类型实体（表：biz_cargo_type）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_cargo_type")
public class CargoType extends TenantEntity {

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 类型编码
     */
    private String typeCode;

    /**
     * 展示颜色
     */
    private String color;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;
}
