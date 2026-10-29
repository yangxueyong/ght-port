package com.ganghuitong.port.api.exception;

import com.ganghuitong.port.api.constant.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 *
 * @author yangxueyong
 */
@Getter
public class CustException extends RuntimeException {

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 构造业务异常（默认业务失败码）
     *
     * @param message 错误信息
     */
    public CustException(String message) {
        super(message);
        this.code = ResultCode.FAIL;
    }

    /**
     * 构造带错误码的业务异常
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public CustException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
