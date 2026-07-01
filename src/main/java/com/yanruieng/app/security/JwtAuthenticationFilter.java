package com.yanruieng.app.security;

import com.yanruieng.app.common.BaseContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProperties props;
    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String auth = request.getHeader(props.getHeader());
            if (auth != null && auth.startsWith(props.getPrefix())) {
                try {
                    String token = auth.substring(props.getPrefix().length());
                    Long userId = tokenProvider.parseAccessTokenUserId(token);
                    var authentication = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    BaseContext.setCurrentUserId(userId);
                } catch (Exception ignored) {
                    SecurityContextHolder.clearContext();
                    BaseContext.clear();
                }
            }
            chain.doFilter(request, response);
        } finally {
            BaseContext.clear();
        }
    }
}
