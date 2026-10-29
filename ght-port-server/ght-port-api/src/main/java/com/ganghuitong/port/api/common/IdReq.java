package com.ganghuitong.port.api.common;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用主键入参
 *
 * @author yangxueyong
 */
@Data
public class IdReq implements Serializable {

    /**
     * 主键ID
     */
    @NotBlank(message = "ID不能为空")
    private String id;

    /**
     * 是否强制执行（用于“正在作业/正在运输”等需要用户二次确认的操作）
     */
    private Boolean force;
}
