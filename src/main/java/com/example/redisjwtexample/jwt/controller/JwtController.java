package com.example.redisjwtexample.jwt.controller;

import com.example.redisjwtexample.jwt.dto.TokenDto;
import com.example.redisjwtexample.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jwt")
public class JwtController {

    private final JwtService jwtService;

    @PostMapping("/reissue")
    public TokenDto reissue() {

        return jwtService.reissue();
    }
}
