package com.example.redisjwtexample.jwt.service;

import com.example.redisjwtexample.jwt.helper.JwtHelper;
import com.example.redisjwtexample.jwt.dto.TokenDto;
import com.example.redisjwtexample.redis.entity.RefreshTokenEntity;
import com.example.redisjwtexample.redis.repository.RefreshTokenRepository;
import com.example.redisjwtexample.user.vo.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtHelper jwtHelper;

    public TokenDto jwtLogin(Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String userId = userDetails.getUsername();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        // JWT 토큰 생성
        String accessToken = jwtHelper.generateAccessToken(userId, role);
        String refreshToken = jwtHelper.generateRefreshToken(userId, role);

        saveRedisRefreshToken(refreshToken, userId);

        return new TokenDto(accessToken, refreshToken);
    }

    private void saveRedisRefreshToken(String refreshToken, String userId) {
        Date expiration = jwtHelper.parseToken(refreshToken)
                .getExpiration();

        long remainingTime = expiration.getTime() - new Date().getTime();

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(
                userId,
                refreshToken,
                remainingTime);

        refreshTokenRepository.save(refreshTokenEntity);
    }
}
