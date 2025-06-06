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
 * ?μ°°κ³΅κ³  λ°°μΉ ?μ???€μ?μ€λ§?λ ?΄λ???μ ???€ν?μ΄ 1???λ????΄μ κΉμ????μ°°κ³΅κ³ λ₯??λ³λ‘??μ§
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BidNoticeScheduler {

    private static final String DATE_FORMAT = "yyyyMMdd";

    private final BidNoticeJobRunner bidNoticeJobRunner;
    private final DateRangeGenerator dateRangeGenerator;

    /**
     * λ§€μΌ ?μ (0??0λΆ?0μ΄????€ν?λ ?€μ?μ€λ§ λ©μ??1???λ????΄μ κΉμ???? μ§λ₯??λ³λ‘??λ???μ°°κ³΅κ³  ?μ§ ?μ ?€ν
     */
    @Scheduled(cron = "0 0 15 * * ?") // λ§€μΌ ?μ ???€ν
//    @Scheduled(cron = "0 11 13 28 3 ?") // ?μ€?Έμ©
    public void scheduleBidNoticeFetch() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ¬ ?μ : {}", nowSeoul.toLocalDateTime());

        try {
            log.info("? ?μ°°κ³΅κ³  μ΄κΈ°???μ ?μ");
            bidNoticeJobRunner.runBidInitJob();
            log.info("? ?μ°°κ³΅κ³  μ΄κΈ°???μ μ’λ£");

            // 1????? μ§? ?΄μ  ? μ§ κ³μ°
            LocalDate endDate = nowSeoul.toLocalDate().minusDays(1); // ?΄μ 
            LocalDate startDate = endDate.minusYears(1); // 1????>>>>>>> f5ab307d13eb94292dec8a65dd640396f5df93d6

            // ?λ³ ? μ§ λ²μ ?μ± (YYYYMMDDhhmm ?μ)
            List<DateRange> monthlyRanges = dateRangeGenerator.generateMonthlyRanges(startDate,
                endDate);

            log.info("? ?μ°°κ³΅κ³  ?μ§ ?μ ?μ");
            // ?μ±??κ°??λ³ λ²μ?????λ°°μΉ ?μ ?€ν
            for (DateRange range : monthlyRanges) {
                String startDateStr = range.getStartDate();
                String endDateStr = range.getEndDate();

                log.debug("? ?μ°°κ³΅κ³  ?λ³ ?μ§ ?μ ?μ: {} - {}", startDateStr, endDateStr);
                bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            }
            log.info("? ?μ°°κ³΅κ³  ?μ§ ?μ μ’λ£");

            log.info("? ?μ°°κ³΅κ³  ?λ°?΄νΈ ?μ ?μ");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("? ?μ°°κ³΅κ³  ?λ°?΄νΈ ?μ μ’λ£");

            log.info("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ¬ μ’λ£ : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ§ μ€??€λ₯ λ°μ: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void scheduleBidNoticeFetch8AM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ¬ ?μ : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("? ?μ°°κ³΅κ³  ?μ§ ?μ ?μ");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(8).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("? ?μ°°κ³΅κ³  ?μ§ ?μ μ’λ£");

            log.info("? ?μ°°κ³΅κ³  ?λ°?΄νΈ ?μ ?μ");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("? ?μ°°κ³΅κ³  ?λ°?΄νΈ ?μ μ’λ£");

            log.info("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ¬ μ’λ£ : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ§ μ€??€λ₯ λ°μ: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduleBidNoticeFetch11AM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ¬ ?μ : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("? ?μ°°κ³΅κ³  ?μ§ ?μ ?μ");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(11).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("? ?μ°°κ³΅κ³  ?μ§ ?μ μ’λ£");

            log.info("? ?μ°°κ³΅κ³  ?λ°?΄νΈ ?μ ?μ");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("? ?μ°°κ³΅κ³  ?λ°?΄νΈ ?μ μ’λ£");

            log.info("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ¬ μ’λ£ : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ§ μ€??€λ₯ λ°μ: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 5 * * ?")
    public void scheduleBidNoticeFetch2PM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ¬ ?μ : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("? ?μ°°κ³΅κ³  ?μ§ ?μ ?μ");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(14).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("? ?μ°°κ³΅κ³  ?μ§ ?μ μ’λ£");

            log.info("? ?μ°°κ³΅κ³  ?λ°?΄νΈ ?μ ?μ");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("? ?μ°°κ³΅κ³  ?λ°?΄νΈ ?μ μ’λ£");

            log.info("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ¬ μ’λ£ : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ§ μ€??€λ₯ λ°μ: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void scheduleBidNoticeFetch4PM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ¬ ?μ : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("? ?μ°°κ³΅κ³  ?μ§ ?μ ?μ");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(16).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("? ?μ°°κ³΅κ³  ?μ§ ?μ μ’λ£");

            log.info("? ?μ°°κ³΅κ³  ?λ°?΄νΈ ?μ ?μ");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("? ?μ°°κ³΅κ³  ?λ°?΄νΈ ?μ μ’λ£");

            log.info("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ¬ μ’λ£ : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("? ?μ°°κ³΅κ³  ?μ§ ?€μ?μ€λ§ μ€??€λ₯ λ°μ: {}", e.getMessage(), e);
        }
    }

}
