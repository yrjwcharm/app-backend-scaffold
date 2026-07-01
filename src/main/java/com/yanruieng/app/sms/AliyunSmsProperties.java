package com.yanruieng.app.sms;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsProperties {
    @NotBlank
    private String accessKeyId;
    @NotBlank
    private String accessKeySecret;
    @NotBlank
    private String endpoint = "dysmsapi.aliyuncs.com";
    @NotBlank
    private String regionId = "cn-hangzhou";
    @NotBlank
    private String signName;
    @NotBlank
    private String templateCode;
    @NotBlank
    private String templateParamName = "code";
    @NotBlank
    private String outIdPrefix = "login-code";
    private boolean autoRetryEnabled = true;
    @Min(1)
    private int maxAttempts = 2;
    @Min(1)
    private int connectTimeoutMillis = 3000;
    @Min(1)
    private int readTimeoutMillis = 5000;
}
