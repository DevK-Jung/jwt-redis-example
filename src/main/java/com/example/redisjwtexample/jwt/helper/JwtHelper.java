package com.example.redisjwtexample.jwt.helper;

import com.example.redisjwtexample.jwt.constants.ClaimKeys;
import com.example.redisjwtexample.jwt.constants.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Component
public class JwtHelper {

    private final SecretKey key;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtHelper(@Value("${jwt.secret}") String secret,
                     @Value("${jwt.access-expiration}") Duration accessTokenExpiration,
                     @Value("${jwt.refresh-expiration}") Duration refreshTokenExpiration) {

        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;

    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(String username, String role) {

        return Jwts.builder()
                .issuer("KIM")
                .subject(username)
                .claims(Map.of(
                        ClaimKeys.ROLE.name(), role,
                        ClaimKeys.TOKEN_TYPE.name(), TokenType.ACCESS
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration.toMillis()))
                .signWith(key)
                .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(String username, String role) {
        return Jwts.builder()
                .issuer("KIM")
                .subject(username)
                .claims(Map.of(
                        ClaimKeys.ROLE.name(), role,
                        ClaimKeys.TOKEN_TYPE.name(), TokenType.REFRESH
                ))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration.toMillis()))
                .signWith(key)
                .compact();
    }

    /**
     * JWT 검증 및 파싱
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
