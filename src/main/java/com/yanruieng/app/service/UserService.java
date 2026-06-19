package com.yanruieng.app.service;

import com.yanruieng.app.vo.UserVO;

public interface UserService {
    UserVO currentUser(Long userId);
}
