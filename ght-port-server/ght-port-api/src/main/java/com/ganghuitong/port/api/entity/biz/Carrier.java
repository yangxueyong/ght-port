package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 承运商实体（表：biz_carrier）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_carrier")
public class Carrier extends TenantEntity {

    /**
     * 承运商名称
     */
    private String carrierName;

    /**
     * 简称
     */
    private String shortName;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话（同时作为承运商登录账号）
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
     * 品牌色（用于车辆涂装展示）
     */
    private String brandColor;

    /**
     * 承运商登录账号用户ID
     */
    private String accountUserId;

    /**
     * 状态：ENABLED-启用，DISABLED-停用
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
