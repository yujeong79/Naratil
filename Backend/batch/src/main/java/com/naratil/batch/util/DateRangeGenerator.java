package com.naratil.batch.util;

import com.naratil.batch.dto.nara.DateRange;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 날짜 범위를 생성하는 유틸리티 클래스
 * 특정 기간을 월 단위로 나누어 입찰공고 수집에 사용할 날짜 범위를 생성
 */
@Slf4j
@Component
public class DateRangeGenerator {

    // 시작 날짜용 포맷터 (YYYYMMDDhhmm)
    private static final DateTimeFormatter START_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd0000");

    // 종료 날짜용 포맷터 (YYYYMMDDhhmm)
    private static final DateTimeFormatter END_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd2359");

    /**
     * 시작일부터 종료일까지의 기간을 월 단위로 나누어 DateRange 리스트를 생성
     *
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 월별 DateRange 리스트
     */
    public List<DateRange> generateMonthlyRanges(LocalDate startDate, LocalDate endDate) {
        List<DateRange> dateRanges = new ArrayList<>();

        // 시작일이 속한 월의 첫날
        LocalDate currentMonthFirst = LocalDate.of(startDate.getYear(), startDate.getMonth(), 1);

        // 종료일이 속한 월의 마지막날
        LocalDate endMonthLast = LocalDate.of(endDate.getYear(), endDate.getMonth(), endDate.lengthOfMonth());

        log.debug("🐛 월별 범위 생성 시작. 시작 월: {}, 종료 월: {}",
            currentMonthFirst.format(DateTimeFormatter.ofPattern("yyyy-MM")),
            endMonthLast.format(DateTimeFormatter.ofPattern("yyyy-MM")));

        // 각 월별 범위 생성
        while (!currentMonthFirst.isAfter(endMonthLast)) {
            // 현재 월의 YearMonth 객체
            YearMonth currentMonth = YearMonth.from(currentMonthFirst);

            // 현재 월의 첫날
            LocalDate monthFirstDay = currentMonth.atDay(1);

            // 현재 월의 마지막날
            LocalDate monthLastDay = currentMonth.atEndOfMonth();

            // 시작일이 현재 처리 중인 월보다 뒤에 있는 경우, 시작일로 조정
            LocalDate rangeStart = monthFirstDay.isBefore(startDate) ? startDate : monthFirstDay;

            // 종료일이 현재 처리 중인 월보다 앞에 있는 경우, 종료일로 조정
            LocalDate rangeEnd = monthLastDay.isAfter(endDate) ? endDate : monthLastDay;

            // DateRange 생성 (시작일은 00:00, 종료일은 23:59로 설정)
            String formattedStartDate = rangeStart.format(START_DATE_FORMATTER);
            String formattedEndDate = rangeEnd.format(END_DATE_FORMATTER);

            dateRanges.add(new DateRange(formattedStartDate, formattedEndDate));

            log.debug("🐛 월별 범위 생성: {} ~ {}", formattedStartDate, formattedEndDate);

            // 다음 월로 이동
            currentMonthFirst = currentMonthFirst.plusMonths(1);
        }

        log.debug("총 {}개의 월별 범위 생성 완료", dateRanges.size());
        return dateRanges;
    }

}
