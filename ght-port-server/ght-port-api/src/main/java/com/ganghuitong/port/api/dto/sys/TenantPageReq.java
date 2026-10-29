package com.ganghuitong.port.api.dto.sys;

import com.ganghuitong.port.api.common.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户分页查询入参
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TenantPageReq extends PageReq {

    /**
     * 状态筛选
     */
    private String status;
}
