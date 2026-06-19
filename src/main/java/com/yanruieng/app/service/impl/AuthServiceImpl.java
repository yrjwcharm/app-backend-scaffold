package com.yanruieng.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanruieng.app.dto.LoginDTO;
import com.yanruieng.app.dto.RegisterDTO;
import com.yanruieng.app.entity.User;
import com.yanruieng.app.entity.UserAuth;
import com.yanruieng.app.mapper.UserAuthMapper;
import com.yanruieng.app.mapper.UserMapper;
import com.yanruieng.app.security.JwtProperties;
import com.yanruieng.app.security.JwtTokenProvider;
import com.yanruieng.app.service.AuthService;
import com.yanruieng.app.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO dto) {
        UserAuth exists = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getIdentityType, "password")
                .eq(UserAuth::getIdentifier, dto.getUsername())
                .last("limit 1"));
        if (exists != null) throw new RuntimeException("账号已存在");

        User user = new User();
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank() ? dto.getUsername() : dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        userMapper.insert(user);

        UserAuth auth = new UserAuth();
        auth.setUserId(user.getId());
        auth.setIdentityType("password");
        auth.setIdentifier(dto.getUsername());
        auth.setCredential(passwordEncoder.encode(dto.getPassword()));
        auth.setStatus(1);
        userAuthMapper.insert(auth);

        String token = jwtTokenProvider.createToken(user.getId());
        return new LoginVO(token, user.getId(), jwtProperties.getExpireDays());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        UserAuth auth = userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getIdentityType, "password")
                .eq(UserAuth::getIdentifier, dto.getUsername())
                .eq(UserAuth::getStatus, 1)
                .last("limit 1"));
        if (auth == null || !passwordEncoder.matches(dto.getPassword(), auth.getCredential())) {
            throw new RuntimeException("账号或密码错误");
        }
        String token = jwtTokenProvider.createToken(auth.getUserId());
        return new LoginVO(token, auth.getUserId(), jwtProperties.getExpireDays());
    }
}
