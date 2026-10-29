package com.ganghuitong.port.api.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户实体基类，带租户ID的业务表均继承此类
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends BaseEntity {

    /**
     * 租户ID
     */
    private String tenantId;
}
