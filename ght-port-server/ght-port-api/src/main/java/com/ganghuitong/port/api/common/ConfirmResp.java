package com.ganghuitong.port.api.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 需要用户二次确认的操作返回结构
 *
 * @author yangxueyong
 */
@Data
public class ConfirmResp implements Serializable {

    /**
     * 是否需要用户确认后再次提交（true时操作未执行）
     */
    private Boolean needConfirm;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 操作完成后的业务主键
     */
    private String bizId;

    /**
     * 构建“需要确认”的返回
     *
     * @param message 提示用户的信息
     * @return 需要确认的结果
     */
    public static ConfirmResp confirm(String message) {
        ConfirmResp resp = new ConfirmResp();
        resp.setNeedConfirm(Boolean.TRUE);
        resp.setMessage(message);
        return resp;
    }

    /**
     * 构建“已执行完成”的返回
     *
     * @param bizId 业务主键
     * @return 已完成的结果
     */
    public static ConfirmResp done(String bizId) {
        ConfirmResp resp = new ConfirmResp();
        resp.setNeedConfirm(Boolean.FALSE);
        resp.setBizId(bizId);
        return resp;
    }
}
