package com.yanruieng.app.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret;
    private Integer expireDays = 30;
    private String header = "Authorization";
    private String prefix = "Bearer ";
}
