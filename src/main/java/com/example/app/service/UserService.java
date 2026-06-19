package com.example.app.service;

import com.example.app.vo.UserVO;

public interface UserService {
    UserVO currentUser(Long userId);
}
