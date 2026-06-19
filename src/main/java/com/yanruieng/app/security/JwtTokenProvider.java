package com.yanruieng.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties props;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long userId) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + Duration.ofDays(props.getExpireDays()).toMillis());
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expire)
                .signWith(key())
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }
}
