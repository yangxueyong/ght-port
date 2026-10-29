package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 货物类型保存入参
 *
 * @author yangxueyong
 */
@Data
public class CargoTypeSaveReq implements Serializable {

    /**
     * 主键ID，为空表示新增
     */
    private String id;

    /**
     * 类型名称
     */
    @NotBlank(message = "类型名称不能为空")
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
