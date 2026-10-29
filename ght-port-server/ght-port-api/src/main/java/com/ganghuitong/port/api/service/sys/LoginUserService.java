package com.ganghuitong.port.api.service.sys;

import com.ganghuitong.port.api.context.LoginUser;

/**
 * 登录用户加载服务（带本地缓存）
 *
 * @author yangxueyong
 */
public interface LoginUserService {

    /**
     * 加载登录用户的完整信息（角色、权限），优先读取缓存
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 登录用户信息，用户不存在或已停用时返回null
     */
    LoginUser load(String userId, String tenantId);

    /**
     * 清空登录用户缓存（角色、权限变更后调用）
     */
    void evictAll();
}
