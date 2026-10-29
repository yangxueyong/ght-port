package com.ganghuitong.port.api.vo.sys;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录页租户选项
 *
 * @author yangxueyong
 */
@Data
public class TenantOptionVO implements Serializable {

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;
}
