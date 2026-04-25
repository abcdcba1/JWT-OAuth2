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

    public String generateToken(User user, Duration expriredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expriredAt.toMillis()), user);
    }

    private String makeToken(Date expiry, User user) {
        Date now = new Date();
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .header()                          // 헤더 시작
                .add("typ", "JWT")      // 헤더에 타입 추가
                .and()                             // 헤더 설정 끝내고 빌더로 복귀
                .issuer(jwtProperties.getIssuer()) // 발행자
                .issuedAt(now)                     // 발행 시간 (set 빼고!)
                .expiration(expiry)                // 만료 시간 (setExpriation 아님! expiration임!)
                .subject(user.getEmail())          // 제목 (이메일)
                .claim("id", user.getId())    // 유저 ID 
                .signWith(secretKey)
                .compact();
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

        return new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities), token, authorities);

    }

    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(secretKey)     // setSigningKey 대신 verifyWith
                .build()                   // parserBuilder 방식이 통합되어 build() 호출 필수
                .parseSignedClaims(token)  // parseClaimsJws 대신 parseSignedClaims
                .getPayload();             // getBody() 대신 getPayload() (내용물이라는 뜻으로 변경)
    }
}
