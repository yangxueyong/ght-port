package com.ganghuitong.port.api.entity.sys;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户实体（表：sys_user）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends TenantEntity {

    /**
     * 登录账号
     */
    private String username;

    /**
     * 密码密文
     */
    @JSONField(serialize = false)
    private String password;

    /**
     * 密码盐值
     */
    @JSONField(serialize = false)
    private String salt;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 用户类型：ADMIN-管理员，STAFF-港口员工，CARRIER-承运商，DRIVER-司机，GUARD-门卫
     */
    private String userType;

    /**
     * 关联业务ID（承运商账号关联承运商ID，司机账号关联车辆ID）
     */
    private String refId;

    /**
     * 状态：ENABLED-启用，DISABLED-停用
     */
    private String status;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 备注
     */
    private String remark;
}
