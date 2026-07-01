package com.yanruieng.app.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtProperties());

    @Test
    void accessTokenCanAuthenticateBusinessRequest() {
        JwtTokenProvider.JwtToken accessToken = jwtTokenProvider.createAccessToken(1001L);

        assertEquals(1001L, jwtTokenProvider.parseAccessTokenUserId(accessToken.value()));
    }

    @Test
    void refreshTokenCannotAuthenticateBusinessRequest() {
        JwtTokenProvider.JwtToken refreshToken = jwtTokenProvider.createRefreshToken(1001L);

        assertThrows(JwtException.class, () -> jwtTokenProvider.parseAccessTokenUserId(refreshToken.value()));
    }

    @Test
    void accessTokenCannotRefreshTokenPair() {
        JwtTokenProvider.JwtToken accessToken = jwtTokenProvider.createAccessToken(1001L);

        assertThrows(JwtException.class, () -> jwtTokenProvider.parseRefreshToken(accessToken.value()));
    }

    private static JwtProperties jwtProperties() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("app-backend-scaffold-jwt-secret-please-change-32bytes");
        properties.setAccessTokenExpireDays(30);
        properties.setRefreshTokenExpireDays(30);
        return properties;
    }
}
