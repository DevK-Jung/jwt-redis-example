//package com.example.redisjwtexample.jwt.filters;
//
//import com.example.redisjwtexample.jwt.helper.JwtHelper;
//import com.example.redisjwtexample.jwt.dto.JwtLoginRespRecord;
//import com.example.redisjwtexample.user.vo.CustomUserDetails;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.micrometer.common.util.StringUtils;
//import jakarta.annotation.PostConstruct;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import java.io.IOException;
//
//@RequiredArgsConstructor
//public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {
//
//    private final AuthenticationManager authenticationManager;
//    private final JwtHelper jwtHelper;
//    private final ObjectMapper objectMapper;
//
//    @PostConstruct
//    public void init() {
//        setFilterProcessesUrl("/api/v1/login");
//    }
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
//        String username = obtainUsername(request);
//        String password = obtainPassword(request);
//
//        if (StringUtils.isBlank(username) || StringUtils.isBlank(password))
//            throw new IllegalArgumentException("Invalid parameter: username or password is missing");
//
//        // Username과 Password 기반으로 인증 토큰 생성 및 인증 시도
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password, null);
//
//        return authenticationManager.authenticate(authenticationToken);
//    }
//
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request,
//                                            HttpServletResponse response,
//                                            FilterChain chain,
//                                            Authentication authentication) throws IOException {
//
//        // 인증 성공 후 액세스 토큰과 리프레시 토큰 생성
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
//
//        String userId = userDetails.getUsername();
//        String role = userDetails.getAuthorities().iterator().next().getAuthority();
//
//        // 클라이언트에서 전달받은 rememberMe 값 확인
////        boolean rememberMe = Boolean.parseBoolean(request.getParameter(REMEMBER_ME));
//
//        // JWT 토큰 생성
//        String accessToken = jwtHelper.generateAccessToken(userId, role);
//        String refreshToken = jwtHelper.generateRefreshToken(userId, role);
//
//        // RefreshToken 엔티티를 Redis에 저장 (TTL 기반 만료)
//        jwtService.saveRedisRefreshToken();
//
//        // 응답으로 토큰 반환
//        JwtLoginRespRecord loginRespRecord = new JwtLoginRespRecord(userId, accessToken, refreshToken);
//
//        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//
//        objectMapper.writeValue(response.getWriter(), loginRespRecord);
//    }
//
//}
