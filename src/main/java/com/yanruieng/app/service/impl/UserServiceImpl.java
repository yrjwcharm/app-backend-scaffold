package com.yanruieng.app.service.impl;

import com.yanruieng.app.entity.User;
import com.yanruieng.app.mapper.UserMapper;
import com.yanruieng.app.service.UserService;
import com.yanruieng.app.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserVO currentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        return new UserVO(user.getNickName(), user.getAvatarUrl(), user.getStatus());
    }
}
