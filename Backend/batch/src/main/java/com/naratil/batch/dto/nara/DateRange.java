package com.naratil.batch.dto.nara;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 시작 날짜와 종료 날짜를 포함하는 날짜 범위 클래스
 * 입찰공고 수집 배치 작업에 사용되는 날짜 범위를 표현
 */
@Getter
@AllArgsConstructor
@ToString
public class DateRange {
    /**
     * 시작 날짜 (YYYYMMDDhhmm 형식)
     * 예: 202501010000 (2025년 1월 1일 00시 00분)
     */
    private String startDate;

    /**
     * 종료 날짜 (YYYYMMDDhhmm 형식)
     * 예: 202501312359 (2025년 1월 31일 23시 59분)
     */
    private String endDate;
}
