package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;

/**
 * 承运商下拉选项
 *
 * @author yangxueyong
 */
@Data
public class CarrierOptionVO implements Serializable {

    /**
     * 承运商ID
     */
    private String id;

    /**
     * 承运商名称
     */
    private String carrierName;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 品牌色
     */
    private String brandColor;
}
