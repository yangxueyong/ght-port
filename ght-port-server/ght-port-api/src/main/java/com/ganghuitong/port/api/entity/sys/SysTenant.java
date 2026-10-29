package com.ganghuitong.port.api.entity.sys;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 租户实体（表：sys_tenant）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
public class SysTenant extends BaseEntity {

    /**
     * 租户编码（登录时使用）
     */
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;

    /**
     * 联系人
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 状态：ENABLED-启用，DISABLED-停用
     */
    private String status;

    /**
     * 到期时间
     */
    private LocalDateTime expireTime;

    /**
     * 备注
     */
    private String remark;
}
