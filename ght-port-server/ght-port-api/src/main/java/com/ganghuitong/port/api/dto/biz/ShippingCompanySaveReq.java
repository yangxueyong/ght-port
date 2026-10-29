package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 船运公司保存入参
 *
 * @author yangxueyong
 */
@Data
public class ShippingCompanySaveReq implements Serializable {

    /**
     * 主键ID，为空表示新增
     */
    private String id;

    /**
     * 船运公司名称
     */
    @NotBlank(message = "船运公司名称不能为空")
    private String companyName;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
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
     * 备注
     */
    private String remark;
}
