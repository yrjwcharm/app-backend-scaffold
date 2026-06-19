package com.yanruieng.app.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {
    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
    private String domain;
    private String dir;
    private List<String> allowedTypes;
    private Long maxSize;
}