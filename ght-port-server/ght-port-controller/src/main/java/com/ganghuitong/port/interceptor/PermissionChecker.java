package com.ganghuitong.port.interceptor;

import cn.hutool.core.collection.CollUtil;
import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.context.LoginUser;
import com.ganghuitong.port.api.enums.RoleCodeEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 接口权限判断组件
 *
 * @author yangxueyong
 */
@Component
public class PermissionChecker {

    /**
     * 判断登录用户是否满足接口所需权限
     *
     * @param user       登录用户
     * @param permission 接口上的权限注解，可能为空
     * @return true-允许访问
     */
    public boolean permitted(LoginUser user, RequiresPermission permission) {
        if (permission == null) {
            return true;
        }
        if (CollUtil.contains(user.getRoleCodes(), RoleCodeEnum.SUPER_ADMIN.getCode())) {
            return true;
        }
        if (CollUtil.contains(user.getPerms(), CommonConstant.ALL_PERMISSION)) {
            return true;
        }
        return Arrays.stream(permission.value()).anyMatch(p -> CollUtil.contains(user.getPerms(), p));
    }
}
