package com.yanruieng.app.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * 阿里云短信验证码发送实现。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TodoSmsSender implements SmsSender {
    private static final String SUCCESS_CODE = "OK";

    private final SmsCodeProperties smsCodeProperties;
    private final AliyunSmsProperties aliyunSmsProperties;
    private final ObjectMapper objectMapper;

    private volatile Client client;

    @Override
    public void sendSmsCode(String phone, String code) {
        String maskedPhone = maskPhone(phone);
        SendSmsRequest request = buildRequest(phone, code);
        if (smsCodeProperties.isLogCodeEnabled()) {
            log.warn("[阿里云短信] 向手机号 {} 发送登录验证码：{}（联调日志，生产环境必须关闭）", maskedPhone, code);
        }

        SendSmsResponse response = callAliyun(request, maskedPhone);
        assertSendSuccess(response, maskedPhone);
    }

    private SendSmsRequest buildRequest(String phone, String code) {
        return new SendSmsRequest()
                .setPhoneNumbers(phone)
                .setSignName(aliyunSmsProperties.getSignName())
                .setTemplateCode(aliyunSmsProperties.getTemplateCode())
                .setTemplateParam(buildTemplateParam(code))
                .setOutId(buildOutId());
    }

    private String buildTemplateParam(String code) {
        try {
            return objectMapper.writeValueAsString(Map.of(aliyunSmsProperties.getTemplateParamName(), code));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("构建短信模板参数失败", e);
        }
    }

    private SendSmsResponse callAliyun(SendSmsRequest request, String maskedPhone) {
        try {
            return aliyunClient().sendSmsWithOptions(request, runtimeOptions());
        } catch (Exception e) {
            log.error("调用阿里云短信接口失败，手机号：{}", maskedPhone, e);
            throw new IllegalStateException("调用阿里云短信接口失败", e);
        }
    }

    private void assertSendSuccess(SendSmsResponse response, String maskedPhone) {
        SendSmsResponseBody body = response == null ? null : response.getBody();
        if (body != null && SUCCESS_CODE.equals(body.getCode())) {
            log.info("阿里云短信发送成功，手机号：{}，requestId：{}，bizId：{}",
                    maskedPhone, body.getRequestId(), body.getBizId());
            return;
        }

        String providerCode = body == null ? "EMPTY_RESPONSE" : body.getCode();
        String requestId = body == null ? null : body.getRequestId();
        String message = body == null ? null : body.getMessage();
        Integer statusCode = response == null ? null : response.getStatusCode();
        log.warn("阿里云短信发送失败，手机号：{}，httpStatus：{}，providerCode：{}，requestId：{}，message：{}",
                maskedPhone, statusCode, providerCode, requestId, message);
        throw new IllegalStateException("阿里云短信发送失败：" + providerCode);
    }

    private Client aliyunClient() throws Exception {
        Client localClient = client;
        if (localClient != null) {
            return localClient;
        }
        synchronized (this) {
            if (client == null) {
                client = new Client(new Config()
                        .setAccessKeyId(aliyunSmsProperties.getAccessKeyId())
                        .setAccessKeySecret(aliyunSmsProperties.getAccessKeySecret())
                        .setEndpoint(aliyunSmsProperties.getEndpoint())
                        .setRegionId(aliyunSmsProperties.getRegionId())
                        .setConnectTimeout(aliyunSmsProperties.getConnectTimeoutMillis())
                        .setReadTimeout(aliyunSmsProperties.getReadTimeoutMillis()));
            }
            return client;
        }
    }

    private RuntimeOptions runtimeOptions() {
        return new RuntimeOptions()
                .setAutoretry(aliyunSmsProperties.isAutoRetryEnabled())
                .setMaxAttempts(aliyunSmsProperties.getMaxAttempts())
                .setConnectTimeout(aliyunSmsProperties.getConnectTimeoutMillis())
                .setReadTimeout(aliyunSmsProperties.getReadTimeoutMillis());
    }

    private String buildOutId() {
        return aliyunSmsProperties.getOutIdPrefix() + '-' + UUID.randomUUID();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "****";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
