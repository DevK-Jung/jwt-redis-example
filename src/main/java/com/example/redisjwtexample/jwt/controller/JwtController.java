package com.example.redisjwtexample.jwt.controller;

import com.example.redisjwtexample.jwt.dto.ReissueDto;
import com.example.redisjwtexample.jwt.dto.TokenDto;
import com.example.redisjwtexample.jwt.service.JwtService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/jwt")
public class JwtController {

    private final JwtService jwtService;

    @PostMapping("/reissue")
    public TokenDto reissue(@NotBlank @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                            @Validated @RequestBody ReissueDto reissueDto) {
        String accessToken = authorization.split(" ")[1];

        return jwtService.reissue(accessToken, reissueDto);
    }
}
