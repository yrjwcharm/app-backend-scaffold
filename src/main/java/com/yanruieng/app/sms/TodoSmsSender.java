package com.yanruieng.app.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 短信发送占位实现。
 * TODO: 替换为阿里云短信、腾讯云短信等正式实现，并根据供应商回执判断发送是否成功。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TodoSmsSender implements SmsSender {
    private final SmsCodeProperties properties;

    @Override
    public void sendLoginCode(String phone, String code) {
        String maskedPhone = phone.substring(0, 3) + "****" + phone.substring(7);
        if (properties.isLogCodeEnabled()) {
            log.warn("[TODO-短信服务] 向手机号 {} 发送登录验证码：{}（联调日志，生产环境必须关闭）", maskedPhone, code);
        } else {
            log.info("[TODO-短信服务] 已受理手机号 {} 的登录验证码发送请求", maskedPhone);
        }
    }
}
