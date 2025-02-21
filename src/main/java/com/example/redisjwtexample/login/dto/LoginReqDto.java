package com.example.redisjwtexample.login.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginReqDto {
    @NotBlank(message = "userId는 필수 입니다.")
    private String userId;
    @NotBlank(message = "password는 필수 입니다.")
    private String password;
}
