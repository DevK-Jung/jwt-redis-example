package com.example.redisjwtexample.jwt;

import com.example.redisjwtexample.jwt.helper.JwtHelper;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest  // Spring Boot 환경에서 테스트 실행
@ActiveProfiles("test")  // 테스트 환경에서 실행 (test.yml 설정 적용 가능)
class JwtHelperTest {

    @Autowired
    private JwtHelper jwtHelper; // Spring 컨텍스트에서 JwtHelper 빈 주입

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_ROLE = "test";

    @Test
    void testGenerateAccessToken() {
        String accessToken = jwtHelper.generateAccessToken(TEST_USERNAME, TEST_ROLE);
        assertNotNull(accessToken);
        System.out.println("Access Token: " + accessToken);
    }

    @Test
    void testGenerateRefreshToken() {
        String refreshToken = jwtHelper.generateRefreshToken(TEST_USERNAME, TEST_ROLE);
        assertNotNull(refreshToken);
        System.out.println("Refresh Token: " + refreshToken);
    }

    @Test
    void testParseToken() {
        String accessToken = jwtHelper.generateAccessToken(TEST_USERNAME, TEST_ROLE);
        Claims claims = jwtHelper.parseToken(accessToken);

        assertNotNull(claims);
        assertEquals(TEST_USERNAME, claims.getSubject());
        assertEquals("KIM", claims.getIssuer());
    }

    @Test
    void testValidateToken_ValidToken() {
        String accessToken = jwtHelper.generateAccessToken(TEST_USERNAME, TEST_ROLE);
        assertTrue(jwtHelper.validateToken(accessToken));
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.value";
        assertFalse(jwtHelper.validateToken(invalidToken));
    }

    @Test
    void testValidateToken_ExpiredToken() throws InterruptedException {
        // 토큰 만료 시간을 1초로 설정 (테스트용)
        Thread.sleep(1000);  // 1초 대기하여 토큰 만료 유도

        String shortLivedToken = jwtHelper.generateAccessToken(TEST_USERNAME, TEST_ROLE);

        Thread.sleep(2000);  // 2초 대기하여 만료 확정

        assertFalse(jwtHelper.validateToken(shortLivedToken));
    }
}
