package com.ganghuitong.port.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 港慧通系统自定义配置（application.yml 中 ght 前缀）
 *
 * @author yangxueyong
 */
@Data
@Component
@ConfigurationProperties(prefix = "ght")
public class GhtProperties {

    /**
     * JWT签名密钥
     */
    private String jwtSecret;

    /**
     * 令牌有效期（小时）
     */
    private Integer tokenExpireHours;

    /**
     * 前端访问地址（用于生成司机出门条链接）
     */
    private String webBaseUrl;
}
