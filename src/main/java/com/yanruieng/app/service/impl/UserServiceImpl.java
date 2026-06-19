package com.yanruieng.app.service.impl;

import com.yanruieng.app.entity.User;
import com.yanruieng.app.mapper.UserMapper;
import com.yanruieng.app.service.UserService;
import com.yanruieng.app.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    public UserVO currentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        return new UserVO(user.getId(), user.getNickname(), user.getAvatar(), user.getPhone());
    }
}
