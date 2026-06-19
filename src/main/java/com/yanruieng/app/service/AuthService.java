package com.yanruieng.app.service;

import com.yanruieng.app.dto.LoginDTO;
import com.yanruieng.app.dto.RegisterDTO;
import com.yanruieng.app.vo.LoginVO;

public interface AuthService {
    LoginVO register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
}
