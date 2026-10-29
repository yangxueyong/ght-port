package com.ganghuitong.port.handler;

import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.constant.ResultCode;
import com.ganghuitong.port.api.exception.CustException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：统一转换为 R 结构返回
 *
 * @author yangxueyong
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return 失败结果
     */
    @ExceptionHandler(CustException.class)
    public R<Void> handleCust(CustException e) {
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（含 @Valid 请求体校验）
     *
     * @param e 校验异常
     * @return 失败结果（首个字段错误信息）
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBind(BindException e) {
        FieldError error = e.getBindingResult().getFieldError();
        String msg = error == null ? "请求参数不正确" : error.getDefaultMessage();
        log.info("参数校验未通过：{}", msg);
        return R.fail(ResultCode.FAIL, msg);
    }

    /**
     * 处理请求体格式错误
     *
     * @param e 请求体解析异常
     * @return 失败结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.info("请求体解析失败：{}", e.getMessage());
        return R.fail(ResultCode.FAIL, "请求参数格式错误");
    }

    /**
     * 处理未知异常
     *
     * @param e 异常
     * @return 失败结果
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        return R.fail(ResultCode.FAIL, "系统繁忙，请稍后再试");
    }
}
