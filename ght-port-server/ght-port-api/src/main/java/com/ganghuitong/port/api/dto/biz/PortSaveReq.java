package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 港口基础信息保存入参
 *
 * @author yangxueyong
 */
@Data
public class PortSaveReq implements Serializable {

    /**
     * 主键ID，为空表示新增
     */
    private String id;

    /**
     * 港口名称
     */
    @NotBlank(message = "港口名称不能为空")
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
     * 港口简介
     */
    private String description;
}
