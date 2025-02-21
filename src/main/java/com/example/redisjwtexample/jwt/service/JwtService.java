package com.example.redisjwtexample.jwt.service;

import com.example.redisjwtexample.jwt.constants.ClaimKeys;
import com.example.redisjwtexample.jwt.constants.TokenType;
import com.example.redisjwtexample.jwt.dto.ReissueDto;
import com.example.redisjwtexample.jwt.dto.TokenDto;
import com.example.redisjwtexample.jwt.helper.JwtHelper;
import com.example.redisjwtexample.redis.entity.RefreshTokenEntity;
import com.example.redisjwtexample.redis.repository.RefreshTokenRepository;
import com.example.redisjwtexample.user.entity.UserEntity;
import com.example.redisjwtexample.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.micrometer.common.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtHelper jwtHelper;

    private final UserRepository userRepository;

    public TokenDto jwtLogin(@NonNull Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String userId = userDetails.getUsername();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return createTokenDto(userId, role);
    }

    // accessToken, refreshToken 둘 다 재생성 하도록 만들어서 redis에서 최신 refreshToken만 유지
    // 토큰 탈취로 이전 refreshToken으로 재발급 수행 시 에러 발생하도록 구현
    public TokenDto reissue(String accessToken, ReissueDto reissueDto) {

        // accessToken 만료 여부 체크
        if (jwtHelper.validateToken(accessToken)) throw new IllegalArgumentException("AccessToken이 만료되지 않았습니다.");

        String userId = validateRefreshToken(reissueDto);

        // 사용자 정보 재조회
        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow();

        // JWT 토큰 생성
        return createTokenDto(userId, user.getRole());

    }

    private String validateRefreshToken(ReissueDto reissueDto) {
        Claims claims = jwtHelper.parseToken(reissueDto.getRefreshToken());
        String tokenType = getValueFromClaims(claims, ClaimKeys.TOKEN_TYPE.name(), String.class);

        // refreshToken 아니라면 에러 발생
        if (!TokenType.REFRESH.name().equals(tokenType)) throw new IllegalArgumentException("Refresh Token이 아닙니다.");

        String userId = getUserIdByClaims(claims);
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token입니다."));

        if (!refreshTokenEntity.getRefreshToken().equals(reissueDto.getRefreshToken()))
            throw new IllegalArgumentException("다른 환경에서 로그인한 이력이 있어 재인증이 필요합니다.");

        return userId;
    }

    public void jwtLogout(@NonNull String refreshToken) {

        if (StringUtils.isBlank(refreshToken)) throw new IllegalArgumentException();

        try {
            String userId = getUserIdByToken(refreshToken);

            refreshTokenRepository.deleteByUserId(userId);
        } catch (ExpiredJwtException e) { // 이미 만료된 토큰이면 return
            log.debug(">>> 이미 만료된 토큰");
        }
    }

    private TokenDto createTokenDto(String userId, String role) {
        // JWT 토큰 생성
        String accessToken = jwtHelper.generateAccessToken(userId, role);
        String refreshToken = jwtHelper.generateRefreshToken(userId, role);

        saveRedisRefreshToken(refreshToken, userId);

        return new TokenDto(accessToken, refreshToken);
    }

    private String getUserIdByToken(String token) {
        return jwtHelper.parseToken(token)
                .getSubject();
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

    private String getUserIdByClaims(Claims claims) {
        return claims.getSubject();
    }

    public Claims getClaimsByToken(@NonNull String token) {
        Objects.requireNonNull(token);

        return jwtHelper.parseToken(token);
    }
}
