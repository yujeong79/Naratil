package com.naratil.user.dto.signup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequestDto(
        @NotBlank String name,              // 유저 이름
        @NotBlank String password,          // 비밀번호
        @Email @NotBlank String email,      // 이메일
        @NotBlank String phone              // 폰번호
) {}
