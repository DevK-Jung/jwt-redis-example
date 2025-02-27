package com.example.redisjwtexample.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CookieUtils {

    /**
     * 쿠키 세팅
     *
     * @param key           쿠키 명
     * @param value         쿠키 값
     * @param path          접근 가능 경로
     * @param maxAgeSeconds 만료 시간 (초 단위)
     * @param isSecure      HTTPS 에서만 전송 가능 여부
     */
    public void setCookie(String key,
                          String value,
                          String path,
                          long maxAgeSeconds,
                          boolean isSecure) {

        HttpServletResponse response = ServletUtils.getServletResponse().orElseThrow();

        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(isSecure); // JavaScript에서 접근 불가 (XSS 방어)
        cookie.setSecure(isSecure); // HTTPS에서만 전송 (보안 강화)
        cookie.setPath(path); // 모든 경로에서 접근 가능
        cookie.setMaxAge((int) maxAgeSeconds); // 만료 시간 설정 (초 단위)

        response.addCookie(cookie);
    }

    /**
     * 쿠키 조회
     *
     * @param key 쿠키 명
     * @return value 쿠키 값
     */
    public String getCookie(String key) {

        HttpServletRequest request = ServletUtils.getServletRequest().orElseThrow();

        Cookie[] cookies = request.getCookies();

        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (key.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    /**
     * 쿠키 삭제
     *
     * @param key 쿠키 명
     */
    public void deleteCookie(String key) {

        HttpServletResponse response = ServletUtils.getServletResponse().orElseThrow();

        Cookie cookie = new Cookie(key, "");

        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // 즉시 만료

        response.addCookie(cookie);
    }

}