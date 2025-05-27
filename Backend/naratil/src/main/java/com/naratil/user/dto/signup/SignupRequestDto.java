package com.naratil.user.dto.signup;

import com.naratil.corporation.dto.CorpRequestDto;
import jakarta.validation.constraints.NotNull;

public record SignupRequestDto(
        @NotNull UserRequestDto account,
        @NotNull CorpRequestDto company,
        @NotNull InterestsDto interests
) {}
