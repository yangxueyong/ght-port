package com.ganghuitong.port.api.common;

import com.ganghuitong.port.api.constant.ResultCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一接口返回结构
 *
 * @param <T> 返回数据类型
 * @author yangxueyong
 */
@Data
public class R<T> implements Serializable {

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 提示信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 构建成功返回（无数据）
     *
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 构建成功返回
     *
     * @param data 返回数据
     * @param <T>  数据类型
     * @return 成功结果
     */
    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(ResultCode.SUCCESS);
        r.setMsg(ResultCode.SUCCESS_MSG);
        r.setData(data);
        return r;
    }

    /**
     * 构建失败返回
     *
     * @param code 错误码
     * @param msg  错误信息
     * @param <T>  数据类型
     * @return 失败结果
     */
    public static <T> R<T> fail(Integer code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
