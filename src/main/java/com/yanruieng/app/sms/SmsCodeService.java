package com.yanruieng.app.sms;

public interface SmsCodeService {
    void sendLoginCode(String phone, String clientIp);

    String verifyAndClaimLoginCode(String phone, String code);

    void consumeClaimedLoginCode(String phone, String claimId);

    void releaseClaimedLoginCode(String phone, String code, String claimId);
}
