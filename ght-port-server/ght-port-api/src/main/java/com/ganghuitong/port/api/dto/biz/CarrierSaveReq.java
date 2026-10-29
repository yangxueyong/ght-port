package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 承运商保存入参
 *
 * @author yangxueyong
 */
@Data
public class CarrierSaveReq implements Serializable {

    /**
     * 主键ID，为空表示新增
     */
    private String id;

    /**
     * 承运商名称
     */
    @NotBlank(message = "承运商名称不能为空")
    private String carrierName;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 联系人
     */
    @NotBlank(message = "联系人不能为空")
    private String contactName;

    /**
     * 联系电话（作为承运商登录账号）
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "请输入正确的手机号")
    private String contactPhone;

    /**
     * 地址
     */
    private String address;

    /**
     * 统一社会信用代码
     */
    private String creditCode;

    /**
     * 品牌色
     */
    private String brandColor;

    /**
     * 状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
