package com.yanruieng.app.sms;

import com.yanruieng.app.common.CustomException;
import com.yanruieng.app.common.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSmsCodeService implements SmsCodeService {
    private static final String KEY_PREFIX = "auth:sms:login:";
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Shanghai");
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final DefaultRedisScript<Long> RESERVE_PHONE_QUOTA_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('exists', KEYS[1]) == 1 then return -1 end
            local daily = tonumber(redis.call('get', KEYS[2]) or '0')
            if daily >= tonumber(ARGV[2]) then return -2 end
            redis.call('set', KEYS[1], '1', 'EX', ARGV[1])
            daily = redis.call('incr', KEYS[2])
            if daily == 1 then redis.call('expire', KEYS[2], ARGV[3]) end
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> RESERVE_IP_QUOTA_SCRIPT = new DefaultRedisScript<>("""
            local hourly = tonumber(redis.call('get', KEYS[1]) or '0')
            if hourly >= tonumber(ARGV[1]) then return -3 end
            hourly = redis.call('incr', KEYS[1])
            if hourly == 1 then redis.call('expire', KEYS[1], ARGV[2]) end
            return 1
            """, Long.class);

    private static final String CLAIM_PREFIX = "claimed:";

    private static final DefaultRedisScript<Long> VERIFY_AND_CLAIM_SCRIPT = new DefaultRedisScript<>("""
            local stored = redis.call('get', KEYS[1])
            if not stored then return 0 end
            if string.sub(stored, 1, string.len(ARGV[4])) == ARGV[4] then return -3 end
            local failures = tonumber(redis.call('get', KEYS[2]) or '0')
            if failures >= tonumber(ARGV[2]) then
                redis.call('del', KEYS[1])
                return -2
            end
            if stored == ARGV[1] then
                local ttl = redis.call('pttl', KEYS[1])
                if ttl <= 0 then
                    redis.call('del', KEYS[1])
                    return 0
                end
                redis.call('set', KEYS[1], ARGV[4] .. ARGV[5], 'PX', ttl)
                return 1
            end
            failures = redis.call('incr', KEYS[2])
            if failures == 1 then redis.call('expire', KEYS[2], ARGV[3]) end
            if failures >= tonumber(ARGV[2]) then
                redis.call('del', KEYS[1])
                return -2
            end
            return -1
            """, Long.class);

    private static final DefaultRedisScript<Long> CONSUME_CLAIM_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('get', KEYS[1]) ~= ARGV[1] then return 0 end
            redis.call('del', KEYS[1], KEYS[2])
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> RELEASE_CLAIM_SCRIPT = new DefaultRedisScript<>("""
            if redis.call('get', KEYS[1]) ~= ARGV[1] then return 0 end
            local ttl = redis.call('pttl', KEYS[1])
            if ttl <= 0 then
                redis.call('del', KEYS[1])
                return 0
            end
            redis.call('set', KEYS[1], ARGV[2], 'PX', ttl)
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> ROLLBACK_PHONE_QUOTA_SCRIPT = new DefaultRedisScript<>("""
            redis.call('del', KEYS[1])
            local value = tonumber(redis.call('get', KEYS[2]) or '0')
            if value <= 1 then redis.call('del', KEYS[2]) else redis.call('decr', KEYS[2]) end
            return 1
            """, Long.class);

    private static final DefaultRedisScript<Long> ROLLBACK_COUNTER_SCRIPT = new DefaultRedisScript<>("""
            local value = tonumber(redis.call('get', KEYS[1]) or '0')
            if value <= 1 then redis.call('del', KEYS[1]) else redis.call('decr', KEYS[1]) end
            return 1
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final SmsCodeProperties properties;
    private final SmsSender smsSender;

    @Override
    public void sendLoginCode(String phone, String clientIp) {
        validateProperties();
        LocalDateTime now = LocalDateTime.now(BUSINESS_ZONE);
        String phoneKeyPrefix = phoneKeyPrefix(phone);
        String lockKey = phoneKeyPrefix + "send-lock";
        String dailyKey = phoneKeyPrefix + "daily:" + now.toLocalDate();
        String ipHash = shortHash(clientIp == null ? "unknown" : clientIp);
        String ipKey = KEY_PREFIX + "ip:{" + ipHash + "}:hour:"
                + now.format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

        long dailyTtl = Math.max(60, Duration.between(now,
                LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.MIDNIGHT)).getSeconds());
        Long phoneQuotaResult;
        try {
            phoneQuotaResult = redisTemplate.execute(RESERVE_PHONE_QUOTA_SCRIPT,
                    List.of(lockKey, dailyKey),
                    String.valueOf(properties.getSendIntervalSeconds()),
                    String.valueOf(properties.getMaxSendsPerDay()),
                    String.valueOf(dailyTtl));
        } catch (DataAccessException e) {
            log.error("Redis 验证码发送配额检查失败", e);
            throw new CustomException(ResponseCode.FAIL, "验证码服务暂不可用，请稍后重试");
        }
        handleQuotaResult(phoneQuotaResult);

        Long ipQuotaResult;
        try {
            ipQuotaResult = redisTemplate.execute(RESERVE_IP_QUOTA_SCRIPT, List.of(ipKey),
                    String.valueOf(properties.getMaxSendsPerIpHour()), "3700");
        } catch (DataAccessException e) {
            rollbackPhoneQuota(lockKey, dailyKey);
            log.error("Redis 验证码 IP 配额检查失败", e);
            throw new CustomException(ResponseCode.FAIL, "验证码服务暂不可用，请稍后重试");
        }
        if (ipQuotaResult == null || ipQuotaResult != 1) {
            rollbackPhoneQuota(lockKey, dailyKey);
            handleQuotaResult(ipQuotaResult);
        }

        String code = generateCode();
        String codeKey = codeKey(phone);
        String failureKey = failureKey(phone);
        try {
            redisTemplate.opsForValue().set(codeKey, digest(phone, code),
                    Duration.ofMinutes(properties.getExpireMinutes()));
            redisTemplate.delete(failureKey);
            smsSender.sendLoginCode(phone, code);
        } catch (Exception e) {
            rollbackFailedSend(codeKey, lockKey, dailyKey, ipKey);
            log.error("短信验证码发送失败，手机号后四位：{}", phone.substring(phone.length() - 4), e);
            throw new CustomException(ResponseCode.FAIL, "验证码发送失败，请稍后重试");
        }
    }

    @Override
    public String verifyAndClaimLoginCode(String phone, String code) {
        validateProperties();
        String claimId = UUID.randomUUID().toString();
        Long result;
        try {
            result = redisTemplate.execute(VERIFY_AND_CLAIM_SCRIPT,
                    List.of(codeKey(phone), failureKey(phone)),
                    digest(phone, code),
                    String.valueOf(properties.getMaxVerifyAttempts()),
                    String.valueOf(Duration.ofMinutes(properties.getExpireMinutes()).toSeconds()),
                    CLAIM_PREFIX,
                    claimId);
        } catch (DataAccessException e) {
            log.error("Redis 验证码校验失败", e);
            throw new CustomException(ResponseCode.FAIL, "验证码服务暂不可用，请稍后重试");
        }

        if (result == null || result == 0) {
            throw new CustomException("验证码已失效，请重新获取");
        }
        if (result == -2) {
            throw new CustomException("验证码错误次数过多，请重新获取");
        }
        if (result == -3) {
            throw new CustomException("登录请求正在处理中，请勿重复提交");
        }
        if (result != 1) {
            throw new CustomException("验证码错误");
        }
        return claimId;
    }

    @Override
    public void consumeClaimedLoginCode(String phone, String claimId) {
        try {
            Long result = redisTemplate.execute(CONSUME_CLAIM_SCRIPT,
                    List.of(codeKey(phone), failureKey(phone)),
                    claimedValue(claimId));
            if (result == null || result != 1) {
                log.warn("验证码占用记录已失效，手机号后四位：{}", phone.substring(phone.length() - 4));
            }
        } catch (DataAccessException e) {
            // 数据库已经提交，保留 claimed 值直至原 TTL 到期，防止验证码被再次使用。
            log.error("登录成功后销毁验证码占用记录失败，手机号后四位：{}",
                    phone.substring(phone.length() - 4), e);
        }
    }

    @Override
    public void releaseClaimedLoginCode(String phone, String code, String claimId) {
        try {
            redisTemplate.execute(RELEASE_CLAIM_SCRIPT, List.of(codeKey(phone)),
                    claimedValue(claimId), digest(phone, code));
        } catch (DataAccessException e) {
            // 释放失败时让占用记录按验证码原 TTL 自然过期，避免验证码被重复消费。
            log.error("登录回滚后恢复验证码失败，手机号后四位：{}",
                    phone.substring(phone.length() - 4), e);
        }
    }

    private void handleQuotaResult(Long result) {
        if (result != null && result == 1) {
            return;
        }
        if (result == null) {
            throw new CustomException(ResponseCode.FAIL, "验证码服务暂不可用，请稍后重试");
        }
        if (result == -1) {
            throw new CustomException(ResponseCode.TOO_MANY_REQUESTS, "验证码发送过于频繁，请稍后再试");
        }
        if (result == -2) {
            throw new CustomException(ResponseCode.TOO_MANY_REQUESTS, "该手机号今日发送次数已达上限");
        }
        if (result == -3) {
            throw new CustomException(ResponseCode.TOO_MANY_REQUESTS, "当前网络请求次数过多，请稍后再试");
        }
        throw new CustomException(ResponseCode.FAIL, "验证码服务暂不可用，请稍后重试");
    }

    private void rollbackFailedSend(String codeKey, String lockKey, String dailyKey, String ipKey) {
        try {
            redisTemplate.delete(codeKey);
            redisTemplate.execute(ROLLBACK_PHONE_QUOTA_SCRIPT, List.of(lockKey, dailyKey));
            redisTemplate.execute(ROLLBACK_COUNTER_SCRIPT, List.of(ipKey));
        } catch (DataAccessException rollbackException) {
            log.error("短信发送失败后的 Redis 配额回滚失败", rollbackException);
        }
    }

    private void rollbackPhoneQuota(String lockKey, String dailyKey) {
        try {
            redisTemplate.execute(ROLLBACK_PHONE_QUOTA_SCRIPT, List.of(lockKey, dailyKey));
        } catch (DataAccessException rollbackException) {
            log.error("短信验证码手机号配额回滚失败", rollbackException);
        }
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, properties.getLength());
        return String.format(Locale.ROOT, "%0" + properties.getLength() + "d", SECURE_RANDOM.nextInt(bound));
    }

    private String digest(String phone, String code) {
        return sha256(phone + ':' + code + ':' + properties.getHashSecret());
    }

    private String claimedValue(String claimId) {
        return CLAIM_PREFIX + claimId;
    }

    private String shortHash(String value) {
        return sha256(value).substring(0, 16);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JVM 不支持 SHA-256", e);
        }
    }

    private String codeKey(String phone) {
        return phoneKeyPrefix(phone) + "code";
    }

    private String failureKey(String phone) {
        return phoneKeyPrefix(phone) + "failures";
    }

    private String phoneKeyPrefix(String phone) {
        // Redis Cluster 中，同一手机号相关 Lua KEY 通过 hash tag 固定在同一 slot。
        return KEY_PREFIX + "phone:{" + phone + "}:";
    }

    private void validateProperties() {
        if (properties.getHashSecret() == null || properties.getHashSecret().length() < 16) {
            throw new IllegalStateException("app.sms-code.hash-secret 至少需要 16 个字符");
        }
        if (properties.getLength() != 6) {
            throw new IllegalStateException("当前接口约定验证码长度必须为 6");
        }
        if (properties.getExpireMinutes() <= 0
                || properties.getSendIntervalSeconds() <= 0
                || properties.getMaxVerifyAttempts() <= 0
                || properties.getMaxSendsPerDay() <= 0
                || properties.getMaxSendsPerIpHour() <= 0) {
            throw new IllegalStateException("app.sms-code 的时效和限流参数必须大于 0");
        }
    }
}
