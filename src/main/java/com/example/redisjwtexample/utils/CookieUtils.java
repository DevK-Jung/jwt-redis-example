package com.example.redisjwtexample.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CookieUtils {

    public void setRefreshTokenCookie(String name,
                                      String refreshToken,
                                      long maxAgeSeconds,
                                      boolean isSecure) {

        HttpServletResponse response = ServletUtils.getServletResponse();

        Cookie cookie = new Cookie(name, refreshToken);
        cookie.setHttpOnly(true); // JavaScript에서 접근 불가 (XSS 방어)
        cookie.setSecure(isSecure); // HTTPS에서만 전송 (보안 강화)
        cookie.setPath("/"); // 모든 경로에서 접근 가능
        cookie.setMaxAge((int) maxAgeSeconds); // 만료 시간 설정 (초 단위)

        response.addCookie(cookie);
    }

    public String getCookie(String name) {

        HttpServletRequest request = ServletUtils.getServletRequest();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Refresh Token 쿠키 삭제 (로그아웃 시 사용)
     */
    public void deleteCookie(String name) {

        HttpServletResponse response = ServletUtils.getServletResponse();

        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료

        response.addCookie(cookie);
    }

}