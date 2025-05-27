package com.naratil.bid.dto;

import java.time.LocalDate;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

public record BidSearchRequestDto(
        String bidNum,                  // 공고번호 검색
        String bidName,                 // 공고명 검색
        String taskType,                // 업무 구분
        Long indstryty,               // 업종
        String ntceInsttNm,            // 공고기관
        String dminsttNm,              // 수요기관
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate minDate,              // 최소 입찰 게시일
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate maxDate,              // 최대 입찰 게시일
        Long minPresmptPrce,            // 최소 추정 금액 ex) 1만원
        Long maxPresmptPrce,            // 최대 추정 금액 ex) 1000만원
        String region,                  // 지역 ex) 서울, 경기, 경남
        String sort                     // 정렬 기준

) {
        // minDate는 00:00:00으로 변환
        public LocalDateTime getMinDateTime() {
                return minDate != null ? minDate.atStartOfDay() : null;
        }

        // maxDate는 23:59:59로 변환
        public LocalDateTime getMaxDateTime() {
                return maxDate != null ? maxDate.atTime(23, 59, 59) : null;
        }
}
