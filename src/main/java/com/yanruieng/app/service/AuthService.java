package com.yanruieng.app.service;

import com.yanruieng.app.dto.PhoneCodeLoginDTO;
import com.yanruieng.app.vo.LoginVO;

public interface AuthService {
    void sendPhoneLoginCode(String phone, String clientIp);

    LoginVO phoneLogin(PhoneCodeLoginDTO dto);
}
