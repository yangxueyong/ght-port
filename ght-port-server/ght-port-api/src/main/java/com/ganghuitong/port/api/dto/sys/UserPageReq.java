package com.ganghuitong.port.api.dto.sys;

import com.ganghuitong.port.api.common.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户分页查询入参
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageReq extends PageReq {

    /**
     * 用户类型筛选
     */
    private String userType;

    /**
     * 状态筛选
     */
    private String status;
}
