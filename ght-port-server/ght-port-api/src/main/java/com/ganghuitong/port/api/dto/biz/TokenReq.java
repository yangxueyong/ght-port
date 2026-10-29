package com.ganghuitong.port.api.dto.biz;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 出门条令牌入参
 *
 * @author yangxueyong
 */
@Data
public class TokenReq implements Serializable {

    /**
     * 出门条访问令牌
     */
    @NotBlank(message = "出门条令牌不能为空")
    private String token;
}
