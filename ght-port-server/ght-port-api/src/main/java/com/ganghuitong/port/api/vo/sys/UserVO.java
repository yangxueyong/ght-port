package com.ganghuitong.port.api.vo.sys;

import com.ganghuitong.port.api.entity.sys.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 用户信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserVO extends SysUser {

    /**
     * 角色ID列表
     */
    private List<String> roleIds;

    /**
     * 角色名称列表
     */
    private List<String> roleNames;
}
