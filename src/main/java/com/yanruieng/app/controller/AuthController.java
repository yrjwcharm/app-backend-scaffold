package com.yanruieng.app.controller;

import com.yanruieng.app.common.ApiResponse;
import com.yanruieng.app.dto.LoginDTO;
import com.yanruieng.app.dto.RegisterDTO;
import com.yanruieng.app.service.AuthService;
import com.yanruieng.app.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return ApiResponse.success(authService.register(dto));
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return ApiResponse.success(authService.login(dto));
    }
}
