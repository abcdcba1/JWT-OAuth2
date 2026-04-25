package com.example.aaa.util;

import java.util.Base64;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

// 저장할 때 (JSON으로 구움)
public static String serialize(Object obj) {
    try {
        // SerializationUtils.serialize 를 절대 쓰지 마세요!
        return Base64.getUrlEncoder()
            .encodeToString(objectMapper.writeValueAsBytes(obj));
    } catch (Exception e) {
        throw new IllegalArgumentException("직렬화 실패", e);
    }
}

// 읽을 때 (JSON으로 읽음)
public static <T> T deserialize(Cookie cookie, Class<T> cls) {
    try {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(cookie.getValue());
        return objectMapper.readValue(decodedBytes, cls);
    } catch (Exception e) {
        // 여전히 '¬' 에러가 난다면, 브라우저에 옛날 쿠키가 남아서 여기로 들어온 것입니다.
        throw new IllegalArgumentException("쿠키 역직렬화 실패", e);
    }
}
}