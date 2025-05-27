package com.naratil.corporation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record CorpRequestDto(
        @NotBlank
        String businessNumber,  // 사업자등록번호
        @NotBlank
        String corpName,        // 업체명
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate openDate,     // 개업 일자
        @NotBlank
        String ceoName,         // 대표자 명
        @NotBlank
        String emplyeNum,        // 종업원 수
        @NotBlank
        Long IndustryCode     // 업종 코드
) {}
