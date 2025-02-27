package com.example.redisjwtexample.jwt.service;

import com.example.redisjwtexample.jwt.constants.ClaimKeys;
import com.example.redisjwtexample.jwt.constants.TokenType;
import com.example.redisjwtexample.jwt.dto.ReissueDto;
import com.example.redisjwtexample.jwt.dto.TokenDto;
import com.example.redisjwtexample.jwt.helper.JwtHelper;
import com.example.redisjwtexample.redis.blacklist.service.AccessTokenBlacklistService;
import com.example.redisjwtexample.redis.refresh.entity.RefreshTokenEntity;
import com.example.redisjwtexample.redis.refresh.repository.RefreshTokenRepository;
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

    public TokenDto jwtLogin(@NonNull Authentication authentication) {

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String userId = userDetails.getUsername();
        String role = userDetails.getAuthorities().iterator().next().getAuthority();

        return generateToken(userId, role);
    }

    private TokenDto generateToken(String userId, String role) {
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
        boolean httpSecure = env.matchesProfiles("prod"); // 운영환경에서 secure 설정

        CookieUtils.setCookie(REFRESH_TOKEN_COOKIE_KEY, refreshToken, "/", remainingTime, httpSecure);
    }

    // accessToken, refreshToken 둘 다 재생성 하도록 만들어서 redis에서 최신 refreshToken만 유지
    // 토큰 탈취로 이전 refreshToken으로 재발급 수행 시 에러 발생하도록 구현
    public TokenDto reissue() {

        String accessToken = getRequestAccessToken();
        String refreshToken = getRequestRefreshToken();

        // token 재발행을 위한 검증
        validateTokenRefresh(accessToken, refreshToken);

        // 사용자 정보 조회 및 검증
        String userId = getUserId(refreshToken);

        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow();

        CustomUserDetails customUserDetails = CustomUserDetails.fromEntity(user);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customUserDetails, null);

        // JWT 토큰 생성
        return jwtLogin(authenticationToken);

    }

    private void validateTokenRefresh(String accessToken, String refreshToken) {
        // accessToken, RefreshToken 빈 값 체크
        if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(refreshToken))
            throw new IllegalArgumentException("token 값은 필수 입니다.");

        // accessToken 만료 확인 - 만료되지 않았으면 에러
        if (jwtHelper.validateToken(accessToken))
            throw new IllegalArgumentException("accessToken 만료 시 갱신 요청 가능합니다.");

        // refreshToken 만료 확인 - 만료됐으면 세션 만료페이지로 이동시켜야함
        if (!isRefreshTokenMatched())
            throw new IllegalArgumentException();
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

    public void jwtLogout() {

        expireRefreshToken();

        expireAccessToken();
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
        String accessToken = getRequestAccessToken();

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

    private String getRequestAccessToken() {
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

    private String getUserIdByClaims(Claims claims) {
        return claims.getSubject();
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

    /**
     * Client로 부터 넘어온 RefreshToken과 로그인 및 재발급시 저장한 refreshToken 을 비교
     *
     * @return 매치여부
     */
    public boolean isRefreshTokenMatched() {

        String refreshToken = getRequestRefreshToken();

        if (StringUtils.isBlank(refreshToken)) throw new IllegalArgumentException();

        String userId = getUserId(refreshToken);

//        if (allowDuplicateLoginIds.contains(userId)) return true; // 중복 로그인 허용 id

        Optional<RefreshTokenEntity> refreshTokenOpt = refreshTokenRepository.findById(userId);

        if (refreshTokenOpt.isEmpty()) throw new IllegalArgumentException();

        return refreshTokenOpt.get().getRefreshToken().equals(refreshToken);
    }

    private String getUserId(String jwtToken) {
        return jwtHelper.parseToken(jwtToken)
                .getSubject();
    }

    public String getRequestRefreshToken() {

        return CookieUtils.getCookie(REFRESH_TOKEN_COOKIE_KEY);
    }
}
