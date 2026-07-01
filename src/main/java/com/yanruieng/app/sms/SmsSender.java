package com.yanruieng.app.sms;

public interface SmsSender {
    void sendSmsCode(String phone, String code);
}
