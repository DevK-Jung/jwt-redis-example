package com.example.redisjwtexample.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutReqDto {
    @NotBlank
    private String refreshToken;
}
