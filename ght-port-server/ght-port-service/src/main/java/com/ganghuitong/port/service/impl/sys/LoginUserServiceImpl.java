package com.ganghuitong.port.service.impl.sys;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.StrUtil;
import com.ganghuitong.port.api.constant.AuthConstant;
import com.ganghuitong.port.api.context.LoginUser;
import com.ganghuitong.port.api.entity.sys.SysMenu;
import com.ganghuitong.port.api.entity.sys.SysRole;
import com.ganghuitong.port.api.entity.sys.SysTenant;
import com.ganghuitong.port.api.entity.sys.SysUser;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.service.sys.LoginUserService;
import com.ganghuitong.port.service.mapper.sys.SysMenuMapper;
import com.ganghuitong.port.service.mapper.sys.SysRoleMapper;
import com.ganghuitong.port.service.mapper.sys.SysTenantMapper;
import com.ganghuitong.port.service.mapper.sys.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户加载服务实现：从数据库组装用户、角色、权限并做本地定时缓存
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserServiceImpl implements LoginUserService {

    /**
     * 登录用户本地缓存（key=用户ID）
     */
    private final TimedCache<String, LoginUser> cache = CacheUtil.newTimedCache(AuthConstant.LOGIN_CACHE_TIMEOUT);

    /**
     * 用户数据访问
     */
    private final SysUserMapper sysUserMapper;

    /**
     * 租户数据访问
     */
    private final SysTenantMapper sysTenantMapper;

    /**
     * 角色数据访问
     */
    private final SysRoleMapper sysRoleMapper;

    /**
     * 菜单数据访问
     */
    private final SysMenuMapper sysMenuMapper;

    /**
     * 加载登录用户的完整信息（角色、权限），优先读取缓存
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 登录用户信息，用户不存在或已停用时返回null
     */
    @Override
    public LoginUser load(String userId, String tenantId) {
        LoginUser cached = cache.get(userId, false);
        if (cached != null && StrUtil.equals(cached.getTenantId(), tenantId)) {
            return cached;
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !StrUtil.equals(EnableStatusEnum.ENABLED.getCode(), user.getStatus())) {
            log.info("用户[{}]不存在或已停用", userId);
            return null;
        }
        SysTenant tenant = sysTenantMapper.selectById(tenantId);
        if (tenant == null || !StrUtil.equals(EnableStatusEnum.ENABLED.getCode(), tenant.getStatus())) {
            log.info("租户[{}]不存在或已停用", tenantId);
            return null;
        }
        LoginUser loginUser = assemble(user, tenant);
        cache.put(userId, loginUser);
        return loginUser;
    }

    /**
     * 清空登录用户缓存（角色、权限变更后调用）
     */
    @Override
    public void evictAll() {
        cache.clear();
    }

    /**
     * 组装登录用户信息
     *
     * @param user   用户实体
     * @param tenant 租户实体
     * @return 登录用户
     */
    private LoginUser assemble(SysUser user, SysTenant tenant) {
        String enabled = EnableStatusEnum.ENABLED.getCode();
        Set<String> roleCodes = sysRoleMapper.selectByUserId(user.getId(), enabled).stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toSet());
        Set<String> perms = sysMenuMapper.selectByUserId(user.getId(), enabled).stream()
                .map(SysMenu::getPerms)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toSet());
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setTenantId(tenant.getId());
        loginUser.setTenantCode(tenant.getTenantCode());
        loginUser.setTenantName(tenant.getTenantName());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setUserType(user.getUserType());
        loginUser.setRefId(user.getRefId());
        loginUser.setRoleCodes(roleCodes);
        loginUser.setPerms(perms);
        return loginUser;
    }
}
