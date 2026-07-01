package com.yanruieng.app.controller;

import com.yanruieng.app.common.ApiResponse;
import com.yanruieng.app.dto.PhoneCodeLoginDTO;
import com.yanruieng.app.dto.PhoneCodeSendDTO;
import com.yanruieng.app.dto.RefreshTokenDTO;
import com.yanruieng.app.service.AuthService;
import com.yanruieng.app.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 发送手机号短信验证码。无论手机号是否已注册，接口响应保持一致，避免泄露账号状态。
     */
    @PostMapping("/sms/code")
    public ApiResponse<Void> sendPhoneCode(@Valid @RequestBody PhoneCodeSendDTO dto,
                                           HttpServletRequest request) {
        authService.sendSmsCode(dto.getPhone(), request.getRemoteAddr());
        return ApiResponse.success();
    }

    /**
     * 手机号验证码登录；手机号首次登录时自动创建用户。
     */
    @PostMapping("/phone/login")
    public ApiResponse<LoginVO> phoneLogin(@Valid @RequestBody PhoneCodeLoginDTO dto) {
        return ApiResponse.success(authService.phoneLogin(dto));
    }

    /**
     * 使用 refreshToken 刷新 accessToken，并轮换新的 refreshToken。
     */
    @PostMapping("/token/refresh")
    public ApiResponse<LoginVO> refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        return ApiResponse.success(authService.refreshToken(dto.getRefreshToken()));
    }
}
