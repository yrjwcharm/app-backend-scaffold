package com.yanruieng.app.controller;

import com.yanruieng.app.common.ApiResponse;
import com.yanruieng.app.service.UserService;
import com.yanruieng.app.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserVO> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(userService.currentUser(userId));
    }
}
