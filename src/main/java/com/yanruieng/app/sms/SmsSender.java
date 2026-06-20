package com.yanruieng.app.sms;

public interface SmsSender {
    void sendLoginCode(String phone, String code);
}
