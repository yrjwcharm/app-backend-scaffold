package com.yanruieng.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties props;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public JwtToken createAccessToken(Long userId) {
        return createToken(userId, TOKEN_TYPE_ACCESS, props.getAccessTokenExpireDays());
    }

    public JwtToken createRefreshToken(Long userId) {
        return createToken(userId, TOKEN_TYPE_REFRESH, props.getRefreshTokenExpireDays());
    }

    private JwtToken createToken(Long userId, String tokenType, Integer expireDays) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + Duration.ofDays(expireDays).toMillis());
        String tokenId = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .id(tokenId)
                .subject(String.valueOf(userId))
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(now)
                .expiration(expire)
                .signWith(key())
                .compact();
        return new JwtToken(token, tokenId);
    }

    public Long parseAccessTokenUserId(String token) {
        JwtTokenClaims claims = parseToken(token);
        if (!TOKEN_TYPE_ACCESS.equals(claims.tokenType())) {
            throw new JwtException("非法accessToken");
        }
        return claims.userId();
    }

    public JwtTokenClaims parseRefreshToken(String token) {
        JwtTokenClaims claims = parseToken(token);
        if (!TOKEN_TYPE_REFRESH.equals(claims.tokenType())) {
            throw new JwtException("非法refreshToken");
        }
        return claims;
    }

    private JwtTokenClaims parseToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        String tokenId = claims.getId();
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
        if (!StringUtils.hasText(tokenId) || !StringUtils.hasText(tokenType)) {
            throw new JwtException("Token缺少必要声明");
        }
        return new JwtTokenClaims(Long.valueOf(claims.getSubject()), tokenId, tokenType);
    }

    public record JwtToken(String value, String tokenId) {
    }

    public record JwtTokenClaims(Long userId, String tokenId, String tokenType) {
    }
}
