package com.example.redisjwtexample.login.dto;

import com.example.redisjwtexample.jwt.dto.TokenDto;

public record LoginRespDto(String userId, TokenDto tokenInfo) {
}
