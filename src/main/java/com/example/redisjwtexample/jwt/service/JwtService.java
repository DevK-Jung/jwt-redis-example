package com.example.redisjwtexample.jwt.service;

import com.example.redisjwtexample.jwt.dto.TokenDto;
import com.example.redisjwtexample.jwt.helper.JwtHelper;
import com.example.redisjwtexample.jwt.redis.blacklist.service.AccessTokenBlacklistService;
import com.example.redisjwtexample.jwt.redis.refresh.entity.RefreshTokenEntity;
import com.example.redisjwtexample.jwt.redis.refresh.repository.RefreshTokenRepository;
import com.example.redisjwtexample.user.entity.UserEntity;
import com.example.redisjwtexample.user.repository.UserRepository;
import com.example.redisjwtexample.user.vo.CustomUserDetails;
import com.example.redisjwtexample.utils.CookieUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.micrometer.common.util.StringUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtHelper jwtHelper;

    private final UserRepository userRepository;

    private final AccessTokenBlacklistService accessTokenBlacklistService;

    private final Environment env;
    private static final String REFRESH_TOKEN_COOKIE_KEY = "refreshToken";

    /**
     * AccessToken, RefreshToken 생성
     * <ul>
     *     <li>RefreshToken: Cookie 및 Redis에 세팅</li>
     * </ul>
     *
     * @param authentication Authentication
     * @return TokenDto(accessToken, refreshToken)
     */
    public TokenDto generateJwtTokens(@NonNull Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String userId = userDetails.getUsername();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return generateJwtTokens(userId, role);
    }

    /**
     * AccessToken, RefreshToken 강제 만료 처리
     * <ul>
     *     <li>AccessToken: Header로 부터 읽어오며 만료되지 않았으면 Redis BlackList에 추가</li>
     *     <li>RefreshToken: Cookie로 부터 읽어오며 만료되지 않았으면 Redis 에서 제거</li>
     * </ul>
     */
    public void expireJwtToken() {

        expireRefreshToken(); // refreshToken 만료 처리 - redis 에서 제거

        expireAccessToken(); // accessToken 만료 처리 - redis에 blacklist로 추가
    }

    /**
     * AccessToken, RefreshToken 재발행 - AccessToken 만료시 호출
     * <ul>
     *     <li>accessToken, refreshToken 둘 다 재생성 하도록 만들어서 redis 에서 최신 refreshToken만 유지</li>
     * </ul>
     *
     * @return TokenDto(accessToken, refreshToken)
     */
    public TokenDto reissue() {


        String accessToken = getRequestAccessTokenFromHeader(); // accessToken 조회
        String refreshToken = getRequestRefreshTokenFromCookie(); // refreshToken 조회

        // token 재발행을 위한 검증
        validateTokenRefresh(accessToken, refreshToken);

        // 사용자 정보 조회 및 검증
        String userId = getUserId(refreshToken);

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow();

        CustomUserDetails customUserDetails = CustomUserDetails.fromEntity(user);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customUserDetails, null);

        // JWT 토큰 생성
        return generateJwtTokens(authenticationToken);

    }

    private TokenDto generateJwtTokens(String userId, String role) {
        // JWT 토큰 생성
        String accessToken = jwtHelper.generateAccessToken(userId, role);
        String refreshToken = jwtHelper.generateRefreshToken(userId, role);

        storeRefreshToken(refreshToken, userId);

        return new TokenDto(accessToken, refreshToken);
    }

    private void storeRefreshToken(String refreshToken, String userId) {
        Date expiration = jwtHelper.parseToken(refreshToken)
                .getExpiration();

        long remainingTime = expiration.getTime() - new Date().getTime();

        saveRefreshTokenInRedis(refreshToken, userId, remainingTime);
        setRefreshTokenCookie(refreshToken, remainingTime / 1000);

    }

    private void saveRefreshTokenInRedis(String refreshToken, String userId, long remainingTime) {
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity(
                userId,
                refreshToken,
                remainingTime);

        refreshTokenRepository.save(refreshTokenEntity);
    }

    private void setRefreshTokenCookie(String refreshToken, long remainingTime) {
        // Refresh Token을 Cookie에 저장
        boolean httpSecure = env.matchesProfiles("prod"); // todo 운영 환경에서 secure 설정

        CookieUtils.setCookie(REFRESH_TOKEN_COOKIE_KEY, refreshToken, "/", remainingTime, httpSecure);
    }

    /**
     * 재발행을 위한 토큰 검증
     *
     * @param accessToken  accessToken
     * @param refreshToken refreshToken
     */
    private void validateTokenRefresh(String accessToken, String refreshToken) {
        // accessToken, RefreshToken 빈 값 체크
        if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(refreshToken))
            throw new IllegalArgumentException("token 값은 필수 입니다.");

        // accessToken 만료 확인 - 만료되지 않았으면 에러
        if (jwtHelper.validateToken(accessToken))
            throw new IllegalArgumentException("accessToken 만료 시 갱신 요청 가능합니다.");

        // refreshToken Redis에 저장되어있는 항목과 비교
        if (!isRefreshTokenValidInRedis(refreshToken))
            throw new IllegalArgumentException("다른 환경에서 로그인한 이력이 있어 재인증이 필요합니다.");
    }

    private void expireRefreshToken() {

        String refreshToken = CookieUtils.getCookie(REFRESH_TOKEN_COOKIE_KEY);

        if (StringUtils.isBlank(refreshToken)) return;

        try {
            String userId = getUserIdByToken(refreshToken);

            // 쿠키 제거
            CookieUtils.deleteCookie(REFRESH_TOKEN_COOKIE_KEY);

            Optional<RefreshTokenEntity> entityOpt = refreshTokenRepository.findById(userId);

            if (entityOpt.isPresent() && refreshToken.equals(entityOpt.get().getRefreshToken()))
                refreshTokenRepository.deleteById(userId);

        } catch (ExpiredJwtException e) { // 이미 만료된 토큰이면 return
            log.debug(">>> 이미 만료된 토큰");
        }
    }

    private void expireAccessToken() {
        String accessToken = getRequestAccessTokenFromHeader();

        if (StringUtils.isBlank(accessToken)) return;

        try {
            Date expiration = jwtHelper.parseToken(accessToken)
                    .getExpiration();

            // redis에AccessToken blackList에 추가
            accessTokenBlacklistService.setBlackList(accessToken, expiration.getTime());

        } catch (JwtException e) {
            // 만료 및 잘못된 토큰은 넘김
            log.error(">> AccessToken 만료 처리 {}", e.getMessage());
        }

    }

    private String getRequestAccessTokenFromHeader() {
        return jwtHelper.getAccessTokenFromHeader();
    }


    private String getUserIdByToken(String token) {
        return jwtHelper.parseToken(token)
                .getSubject();
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

    /**
     * accessToken BlackList인지 확인
     */
    public boolean isBlacklisted(String accessToken) {
        return accessTokenBlacklistService.isBlacklisted(accessToken);
    }


    public boolean isRefreshTokenValidInRedis() {
        String refreshToken = getRequestRefreshTokenFromCookie();

        return isRefreshTokenValidInRedis(refreshToken);
    }

    /**
     * Client로 부터 넘어온 RefreshToken과 로그인 및 재발급시 저장한 refreshToken 을 비교
     *
     * @return 매치여부
     */
    public boolean isRefreshTokenValidInRedis(String refreshToken) {

        if (StringUtils.isBlank(refreshToken)) throw new IllegalArgumentException();

        String userId = getUserId(refreshToken);

        Optional<RefreshTokenEntity> refreshTokenOpt = refreshTokenRepository.findById(userId);

        if (refreshTokenOpt.isEmpty()) throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");

        return refreshTokenOpt.get().getRefreshToken().equals(refreshToken);
    }

    private String getUserId(String jwtToken) {
        return jwtHelper.parseToken(jwtToken)
                .getSubject();
    }

    public String getRequestRefreshTokenFromCookie() {

        return CookieUtils.getCookie(REFRESH_TOKEN_COOKIE_KEY);
    }
}
