package com.yanruieng.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanruieng.app.common.BaseContext;
import com.yanruieng.app.common.CustomException;
import com.yanruieng.app.common.ResponseCode;
import com.yanruieng.app.dto.PhoneCodeLoginDTO;
import com.yanruieng.app.entity.User;
import com.yanruieng.app.entity.UserAuth;
import com.yanruieng.app.mapper.UserAuthMapper;
import com.yanruieng.app.mapper.UserMapper;
import com.yanruieng.app.security.JwtProperties;
import com.yanruieng.app.security.JwtTokenProvider;
import com.yanruieng.app.service.AuthService;
import com.yanruieng.app.sms.SmsCodeService;
import com.yanruieng.app.vo.LoginVO;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String IDENTITY_PHONE = "phone";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "auth:refresh-token:";
    private static final RedisScript<Long> CONSUME_REFRESH_TOKEN_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            end
            return 0
            """, Long.class);

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final SmsCodeService smsCodeService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void sendSmsCode(String phone, String clientIp) {
        smsCodeService.sendSmsCode(phone.trim(), clientIp);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO phoneLogin(PhoneCodeLoginDTO dto) {
        String phone = dto.getPhone().trim();
        String code = dto.getCode();
        // 先原子占用验证码，防止并发重复登录；事务提交后销毁，回滚时恢复剩余有效期。
        String claimId = smsCodeService.verifyAndClaimLoginCode(phone, code);
        registerSmsCodeClaimSynchronization(phone, code, claimId);

        UserAuth phoneAuth = findAuth(phone);
        User user;
        if (phoneAuth == null) {
            user = createPhoneUser(phone);
        } else {
            user = requireActiveUser(phoneAuth.getUserId());
        }
        return completeLogin(user);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        JwtTokenProvider.JwtTokenClaims claims = parseRefreshToken(refreshToken);
        if (!consumeRefreshToken(claims.tokenId(), claims.userId())) {
            throw invalidRefreshTokenException();
        }
        User user = requireActiveUser(claims.userId());
        return issueTokenPair(user.getId());
    }

    private void registerSmsCodeClaimSynchronization(String phone, String code, String claimId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            smsCodeService.releaseClaimedLoginCode(phone, code, claimId);
            throw new IllegalStateException("手机号验证码登录必须在事务中执行");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                smsCodeService.consumeClaimedLoginCode(phone, claimId);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    smsCodeService.releaseClaimedLoginCode(phone, code, claimId);
                }
            }
        });
    }

    private User createPhoneUser(String phone) {
        User user = new User();
        user.setNickName("用户" + phone.substring(phone.length() - 4));
        user.setStatus(1);
        userMapper.insert(user);
        UserAuth phoneAuth = new UserAuth();
        phoneAuth.setUserId(user.getId());
        phoneAuth.setIdentityType(IDENTITY_PHONE);
        phoneAuth.setIdentifier(phone);
        userAuthMapper.insert(phoneAuth);
        return user;
    }

    private UserAuth findAuth(String identifier) {
        return userAuthMapper.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getIdentityType, AuthServiceImpl.IDENTITY_PHONE)
                .eq(UserAuth::getIdentifier, identifier)
                .last("limit 1"));
    }

    private User requireActiveUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new CustomException("用户不存在");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new CustomException(ResponseCode.FORBIDDEN, "账号不可用，请联系管理员");
        }
        return user;
    }

    private LoginVO completeLogin(User user) {
        BaseContext.setCurrentUserId(user.getId());
        try {
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
            return issueTokenPair(user.getId());
        } finally {
            BaseContext.clear();
        }
    }

    private LoginVO issueTokenPair(Long userId) {
        JwtTokenProvider.JwtToken accessToken = jwtTokenProvider.createAccessToken(userId);
        JwtTokenProvider.JwtToken refreshToken = jwtTokenProvider.createRefreshToken(userId);
        storeRefreshToken(refreshToken.tokenId(), userId);
        return new LoginVO(
                accessToken.value(),
                refreshToken.value(),
                userId,
                jwtProperties.getAccessTokenExpireDays(),
                jwtProperties.getRefreshTokenExpireDays()
        );
    }

    private void storeRefreshToken(String tokenId, Long userId) {
        redisTemplate.opsForValue().set(
                refreshTokenKey(tokenId),
                String.valueOf(userId),
                Duration.ofDays(jwtProperties.getRefreshTokenExpireDays())
        );
    }

    private boolean consumeRefreshToken(String tokenId, Long userId) {
        Long result = redisTemplate.execute(
                CONSUME_REFRESH_TOKEN_SCRIPT,
                Collections.singletonList(refreshTokenKey(tokenId)),
                String.valueOf(userId)
        );
        return Long.valueOf(1L).equals(result);
    }

    private JwtTokenProvider.JwtTokenClaims parseRefreshToken(String refreshToken) {
        try {
            return jwtTokenProvider.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw invalidRefreshTokenException();
        }
    }

    private CustomException invalidRefreshTokenException() {
        return new CustomException(ResponseCode.UNAUTHORIZED, "refreshToken无效或已过期");
    }

    private String refreshTokenKey(String tokenId) {
        return REFRESH_TOKEN_KEY_PREFIX + tokenId;
    }
}
