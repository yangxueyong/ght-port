package com.ganghuitong.port.api.vo.sys;

import lombok.Data;

import java.io.Serializable;

/**
 * 令牌解析结果
 *
 * @author yangxueyong
 */
@Data
public class TokenClaimVO implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;
}
