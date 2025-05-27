package com.naratil.user.dto.login;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record UserInfoResponseDto(
        String email,           //유저 이메일
        String name,            // 유저 이름
        String phone,           // 유저 폰번호
        String bizno,           // 사업자등록번호
        String corpName,        // 회사명
        String ceoName,         // 회사 대표명
        LocalDate openDate,     // 회사 설립일
        String employeeCount    // 종업원의수
) {
}
