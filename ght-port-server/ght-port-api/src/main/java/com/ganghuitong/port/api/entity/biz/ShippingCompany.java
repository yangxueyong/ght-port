package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 船运公司实体（表：biz_shipping_company）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_shipping_company")
public class ShippingCompany extends TenantEntity {

    /**
     * 船运公司名称
     */
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
