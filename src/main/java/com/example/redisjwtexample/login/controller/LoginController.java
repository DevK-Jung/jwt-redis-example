package com.example.redisjwtexample.login.controller;

import com.example.redisjwtexample.login.dto.LoginReqDto;
import com.example.redisjwtexample.login.dto.LoginRespDto;
import com.example.redisjwtexample.login.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/api/v1/login")
    public LoginRespDto login(@Validated @RequestBody LoginReqDto loginReqDto) {

        return loginService.login(loginReqDto);
    }

    @PostMapping("/api/v1/logout")
    public void logout() {
        // header에 로그아웃 시킬 accessToken 필요
        // cookie에 로그아웃 시킬 RefreshToken 필요

        loginService.logout();
    }
}
