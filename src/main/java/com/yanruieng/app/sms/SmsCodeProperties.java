package com.yanruieng.app.sms;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@ConfigurationProperties(prefix = "app.sms-code")
@Validated
public class SmsCodeProperties {
    @NotBlank
    @Size(min = 16)
    private String hashSecret;
    @Min(1)
    private int expireMinutes = 5;
    @Min(1)
    private int sendIntervalSeconds = 60;
    @Min(1)
    private int maxVerifyAttempts = 5;
    @Min(1)
    private int maxSendsPerDay = 10;
    @Min(1)
    private int maxSendsPerIpHour = 30;
    @Min(6)
    @Max(6)
    private int length = 6;
    private boolean logCodeEnabled = true;
}
