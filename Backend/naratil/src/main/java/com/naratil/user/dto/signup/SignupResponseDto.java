package com.naratil.user.dto.signup;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record SignupResponseDto(
        @NotBlank
        Boolean success,  // 성공 여부
        @NotBlank
        String message    // 상세 메시지
) {

}
