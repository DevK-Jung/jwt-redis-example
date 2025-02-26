package com.example.redisjwtexample.jwt.filters;


import com.example.redisjwtexample.jwt.constants.ClaimKeys;
import com.example.redisjwtexample.jwt.constants.TokenType;
import com.example.redisjwtexample.jwt.service.JwtService;
import com.example.redisjwtexample.user.vo.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final static String BEARER_PREFIX = "Bearer";

    private final JwtService jwtService;

    @Value("${security.jwt.exclude-urls}")
    private String[] excludeUrls;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = getAccessToken(request);

        validateJwt(token);

        CustomUserDetails userDetails = getUserDetailsByToken(token);

        setAuthentication(userDetails);

        filterChain.doFilter(request, response);
    }

    private String getAccessToken(HttpServletRequest request) {
        String authorization = request.getHeader(AUTHORIZATION);

        if (StringUtils.isBlank(authorization) || !authorization.startsWith(BEARER_PREFIX))
            throw new InsufficientAuthenticationException("JWT token is missing");

        String token = authorization.split(" ")[1];

        if (StringUtils.isBlank(token)) throw new IllegalArgumentException("JWT token is missing");

        return token;
    }

    private CustomUserDetails getUserDetailsByToken(String token) {
        Claims claim = jwtService.getClaimsByToken(token);
        String tokenType = jwtService.getValueFromClaims(claim, ClaimKeys.TOKEN_TYPE.name(), String.class);

        if (!TokenType.ACCESS.name().equals(tokenType)) throw new IllegalArgumentException();

        String userId = claim.getSubject();
        String role = jwtService.getValueFromClaims(claim, ClaimKeys.ROLE.name(), String.class);

        return CustomUserDetails.of(userId, role);
    }

    private void setAuthentication(CustomUserDetails userDetails) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void validateJwt(String accessToken) {
        // accessToken blackList 존재 여부 확인 - 존재하면 재발급 필요
        if (jwtService.isBlacklisted(accessToken))
            throw new ExpiredJwtException(null, null, "강제 만료된 accessToken 재발행 필요");

        // refreshToken 다르면 중복 로그인
        if (!jwtService.isRefreshTokenMatched())
            throw new RuntimeException("중복 로그인");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return Arrays.stream(excludeUrls)
                .anyMatch(v -> request.getRequestURI().equals(v));
    }
}
