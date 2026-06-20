package com.yanruieng.app.controller;

import com.yanruieng.app.common.ApiResponse;
import com.yanruieng.app.service.UserService;
import com.yanruieng.app.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserVO> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(userService.currentUser(userId));
    }
}
