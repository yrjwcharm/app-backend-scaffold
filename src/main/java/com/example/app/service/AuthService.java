package com.example.app.service;

import com.example.app.dto.LoginDTO;
import com.example.app.dto.RegisterDTO;
import com.example.app.vo.LoginVO;

public interface AuthService {
    LoginVO register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
}
