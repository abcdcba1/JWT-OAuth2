package com.example.aaa.config;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.aaa.config.jwt.TokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter { 
    private final TokenProvider tokenProvider;
    private final static String HEADER_AUTHORIZATION = "Authorization";
    private final static String TOKEN_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 要請ヘッダーのAuthrizationのキー値照会
        String authorizationHeader = request.getHeader(HEADER_AUTHORIZATION);
        // 取ったキーの接頭辞除去
        String token = getAccessToken(authorizationHeader);

        // 取ったキーが有効か検証
        if (token != null && tokenProvider.validToken(token)) {
            Authentication authentication = tokenProvider.getAuthentication(token); // トークンからユーザー情報を取る
            SecurityContextHolder.getContext().setAuthentication(authentication); // 取ったユーザー情報をSecurity Contextに貯蔵
        }

        filterChain.doFilter(request, response);
    }

    // 取ったキーの接頭辞除去
    private String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return authorizationHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}