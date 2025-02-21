package com.example.redisjwtexample.jwt.service;

import com.example.redisjwtexample.jwt.dto.TokenDto;
import com.example.redisjwtexample.jwt.helper.JwtHelper;
import com.example.redisjwtexample.redis.entity.RefreshTokenEntity;
import com.example.redisjwtexample.redis.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

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

    public <T> T getValueFromClaims(Claims claims, String key, Class<T> responseType) {
        Object value = claims.get(key);

        if (value == null) throw new IllegalArgumentException("The key '" + key + "' does not exist in claims.");

        if (!responseType.isInstance(value))
            throw new ClassCastException("Cannot cast value of key '" + key + "' to " + responseType.getSimpleName());

        return responseType.cast(value);
    }

    public Claims getClaimsByToken(@NonNull String token) {
        Objects.requireNonNull(token);

        return jwtHelper.parseToken(token);
    }
}
