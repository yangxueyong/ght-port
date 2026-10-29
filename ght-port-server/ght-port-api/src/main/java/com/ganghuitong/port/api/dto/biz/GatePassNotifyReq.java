package com.ganghuitong.port.api.dto.biz;

import lombok.Data;

import java.io.Serializable;

/**
 * 出门条链接通知入参
 *
 * @author yangxueyong
 */
@Data
public class GatePassNotifyReq implements Serializable {

    /**
     * 司机手机号
     */
    private String phone;

    /**
     * 车牌号
     */
    private String plateNo;

    /**
     * 出门条编号
     */
    private String passNo;

    /**
     * 出门条访问链接
     */
    private String link;
}
