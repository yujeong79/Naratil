package com.naratil.user.dto.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record LoginResponseDto(
        @NotBlank Boolean loginResult,
        String accessToken
) { }
