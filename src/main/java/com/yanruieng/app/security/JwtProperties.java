package com.yanruieng.app.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    /**
     * 兼容历史配置；未单独配置 access / refresh 过期时间时使用该值。
     */
    private Integer expireDays = 30;
    private Integer accessTokenExpireDays;
    private Integer refreshTokenExpireDays;
    private String header = "Authorization";
    private String prefix = "Bearer ";

    public Integer getAccessTokenExpireDays() {
        return accessTokenExpireDays == null ? expireDays : accessTokenExpireDays;
    }

    public Integer getRefreshTokenExpireDays() {
        return refreshTokenExpireDays == null ? expireDays : refreshTokenExpireDays;
    }
}
