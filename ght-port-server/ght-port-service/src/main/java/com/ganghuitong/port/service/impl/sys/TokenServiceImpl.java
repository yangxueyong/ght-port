package com.ganghuitong.port.service.impl.sys;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import com.ganghuitong.port.api.constant.AuthConstant;
import com.ganghuitong.port.api.service.sys.TokenService;
import com.ganghuitong.port.api.vo.sys.TokenClaimVO;
import com.ganghuitong.port.service.config.GhtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * 访问令牌服务实现（基于 hutool JWT）
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    /**
     * 系统配置
     */
    private final GhtProperties ghtProperties;

    /**
     * 生成访问令牌
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return JWT令牌字符串
     */
    @Override
    public String createToken(String userId, String tenantId) {
        return JWT.create()
                .setPayload(AuthConstant.CLAIM_USER_ID, userId)
                .setPayload(AuthConstant.CLAIM_TENANT_ID, tenantId)
                .setExpiresAt(DateUtil.offsetHour(DateUtil.date(), ghtProperties.getTokenExpireHours()))
                .setKey(secretBytes())
                .sign();
    }

    /**
     * 解析并校验访问令牌
     *
     * @param token JWT令牌字符串（不含Bearer前缀）
     * @return 令牌中的用户与租户信息，令牌非法或过期时返回null
     */
    @Override
    public TokenClaimVO parseToken(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            JWT jwt = JWT.of(token).setKey(secretBytes());
            if (!jwt.verify() || !jwt.validate(0)) {
                log.info("令牌签名校验失败或已过期");
                return null;
            }
            TokenClaimVO claim = new TokenClaimVO();
            claim.setUserId(Convert.toStr(jwt.getPayload(AuthConstant.CLAIM_USER_ID)));
            claim.setTenantId(Convert.toStr(jwt.getPayload(AuthConstant.CLAIM_TENANT_ID)));
            if (ObjectUtil.hasNull(claim.getUserId(), claim.getTenantId())) {
                log.info("令牌缺少必要载荷");
                return null;
            }
            return claim;
        } catch (Exception e) {
            log.info("令牌解析失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取签名密钥字节
     *
     * @return 密钥字节数组
     */
    private byte[] secretBytes() {
        return ghtProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
    }
}
