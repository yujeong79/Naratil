package com.naratil.batch.scheduler;

import com.naratil.batch.dto.nara.DateRange;
import com.naratil.batch.runner.BidNoticeJobRunner;
import com.naratil.batch.util.DateRangeGenerator;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ?�찰공고 배치 ?�업???��?줄링?�는 ?�래???�정???�행?�어 1???��????�제까�????�찰공고�??�별�??�집
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BidNoticeScheduler {

    private static final String DATE_FORMAT = "yyyyMMdd";

    private final BidNoticeJobRunner bidNoticeJobRunner;
    private final DateRangeGenerator dateRangeGenerator;

    /**
     * 매일 ?�정(0??0�?0�????�행?�는 ?��?줄링 메서??1???��????�제까�????�짜�??�별�??�누???�찰공고 ?�집 ?�업 ?�행
     */
    @Scheduled(cron = "0 0 15 * * ?") // 매일 ?�정???�행
//    @Scheduled(cron = "0 11 13 28 3 ?") // ?�스?�용
    public void scheduleBidNoticeFetch() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("?�� ?�찰공고 ?�집 ?��?줄러 ?�작 : {}", nowSeoul.toLocalDateTime());

        try {
            log.info("?�� ?�찰공고 초기???�업 ?�작");
            bidNoticeJobRunner.runBidInitJob();
            log.info("?�� ?�찰공고 초기???�업 종료");

            // 1?????�짜?� ?�제 ?�짜 계산
            LocalDate endDate = nowSeoul.toLocalDate().minusDays(1); // ?�제
            LocalDate startDate = endDate.minusYears(1); // 1????>>>>>>> f5ab307d13eb94292dec8a65dd640396f5df93d6

            // ?�별 ?�짜 범위 ?�성 (YYYYMMDDhhmm ?�식)
            List<DateRange> monthlyRanges = dateRangeGenerator.generateMonthlyRanges(startDate,
                endDate);

            log.info("?�� ?�찰공고 ?�집 ?�업 ?�작");
            // ?�성??�??�별 범위???�??배치 ?�업 ?�행
            for (DateRange range : monthlyRanges) {
                String startDateStr = range.getStartDate();
                String endDateStr = range.getEndDate();

                log.debug("?�� ?�찰공고 ?�별 ?�집 ?�업 ?�작: {} - {}", startDateStr, endDateStr);
                bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            }
            log.info("?�� ?�찰공고 ?�집 ?�업 종료");

            log.info("?�� ?�찰공고 ?�데?�트 ?�업 ?�작");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("?�� ?�찰공고 ?�데?�트 ?�업 종료");

            log.info("?�� ?�찰공고 ?�집 ?��?줄러 종료 : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("?�� ?�찰공고 ?�집 ?��?줄링 �??�류 발생: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void scheduleBidNoticeFetch8AM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("?�� ?�찰공고 ?�집 ?��?줄러 ?�작 : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("?�� ?�찰공고 ?�집 ?�업 ?�작");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(8).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("?�� ?�찰공고 ?�집 ?�업 종료");

            log.info("?�� ?�찰공고 ?�데?�트 ?�업 ?�작");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("?�� ?�찰공고 ?�데?�트 ?�업 종료");

            log.info("?�� ?�찰공고 ?�집 ?��?줄러 종료 : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("?�� ?�찰공고 ?�집 ?��?줄링 �??�류 발생: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduleBidNoticeFetch11AM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("?�� ?�찰공고 ?�집 ?��?줄러 ?�작 : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("?�� ?�찰공고 ?�집 ?�업 ?�작");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(11).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("?�� ?�찰공고 ?�집 ?�업 종료");

            log.info("?�� ?�찰공고 ?�데?�트 ?�업 ?�작");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("?�� ?�찰공고 ?�데?�트 ?�업 종료");

            log.info("?�� ?�찰공고 ?�집 ?��?줄러 종료 : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("?�� ?�찰공고 ?�집 ?��?줄링 �??�류 발생: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 5 * * ?")
    public void scheduleBidNoticeFetch2PM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("?�� ?�찰공고 ?�집 ?��?줄러 ?�작 : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("?�� ?�찰공고 ?�집 ?�업 ?�작");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(14).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("?�� ?�찰공고 ?�집 ?�업 종료");

            log.info("?�� ?�찰공고 ?�데?�트 ?�업 ?�작");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("?�� ?�찰공고 ?�데?�트 ?�업 종료");

            log.info("?�� ?�찰공고 ?�집 ?��?줄러 종료 : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("?�� ?�찰공고 ?�집 ?��?줄링 �??�류 발생: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void scheduleBidNoticeFetch4PM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("?�� ?�찰공고 ?�집 ?��?줄러 ?�작 : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("?�� ?�찰공고 ?�집 ?�업 ?�작");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(16).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("?�� ?�찰공고 ?�집 ?�업 종료");

            log.info("?�� ?�찰공고 ?�데?�트 ?�업 ?�작");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("?�� ?�찰공고 ?�데?�트 ?�업 종료");

            log.info("?�� ?�찰공고 ?�집 ?��?줄러 종료 : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("?�� ?�찰공고 ?�집 ?��?줄링 �??�류 발생: {}", e.getMessage(), e);
        }
    }

}
