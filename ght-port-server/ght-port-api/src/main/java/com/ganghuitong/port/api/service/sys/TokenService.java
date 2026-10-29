package com.ganghuitong.port.api.service.sys;

import com.ganghuitong.port.api.vo.sys.TokenClaimVO;

/**
 * 访问令牌服务（JWT）
 *
 * @author yangxueyong
 */
public interface TokenService {

    /**
     * 生成访问令牌
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return JWT令牌字符串
     */
    String createToken(String userId, String tenantId);

    /**
     * 解析并校验访问令牌
     *
     * @param token JWT令牌字符串（不含Bearer前缀）
     * @return 令牌中的用户与租户信息，令牌非法或过期时返回null
     */
    TokenClaimVO parseToken(String token);
}
