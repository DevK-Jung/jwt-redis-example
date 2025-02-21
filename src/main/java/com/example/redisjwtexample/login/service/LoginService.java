package com.example.redisjwtexample.login.service;

import com.example.redisjwtexample.jwt.dto.TokenDto;
import com.example.redisjwtexample.jwt.service.JwtService;
import com.example.redisjwtexample.login.dto.LoginReqDto;
import com.example.redisjwtexample.login.dto.LoginRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginService {

    private final JwtService jwtLogin;
    private final AuthenticationManager authenticationManager;

    public LoginRespDto login(LoginReqDto loginReqDto) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginReqDto.getUserId(), loginReqDto.getPassword(), null);

        Authentication authenticate = authenticationManager.authenticate(authenticationToken);

        TokenDto tokenDto = jwtLogin.jwtLogin(authenticate);

        return new LoginRespDto(loginReqDto.getUserId(), tokenDto);
    }
}
