package com.example.aaa.config.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.example.aaa.domain.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final jwtProperties jwtProperties;

    public String generateToken(User user, Duration expriredAt) { // 発給
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expriredAt.toMillis()), user);
    }

    private String makeToken(Date expiry, User user) { // 生成
        Date now = new Date();
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .header() // ヘッダー始まり
                .add("typ", "JWT") // ヘッダーにタイプ追加
                .and()  // ヘッダー設定終わり
                .issuer(jwtProperties.getIssuer()) // トークン管理者（サーバー）
                .issuedAt(now)
                .expiration(expiry)
                .subject(user.getEmail())
                .claim("id", user.getId()) // リクエスト要請者（クライアント）
                .signWith(secretKey)
                .compact(); // 文字列にする
    }

    public boolean validToken(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));

        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        return new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(
            claims.getSubject(), // ユーザー識別、Eメールなど
            "", // パスワード、トークン使用の場合は要らない
            authorities), // 権限リスト、UserかAdminなのかなど
            token, // トークン文字列
            authorities // // 上のとは違って、トークン自体に記録するもの、つまり、トークンだけで検証ができるようにする、権限リスト、UserかAdminなのかなど
        );

    }

    public Long getUserId(String token) { // リクエストのID
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
