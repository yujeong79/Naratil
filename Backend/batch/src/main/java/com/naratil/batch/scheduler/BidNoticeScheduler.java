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
 * ?…ì°°ê³µê³  ë°°ì¹˜ ?‘ì—…???¤ì?ì¤„ë§?˜ëŠ” ?´ë˜???ì •???¤í–‰?˜ì–´ 1???„ë????´ì œê¹Œì????…ì°°ê³µê³ ë¥??”ë³„ë¡??˜ì§‘
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BidNoticeScheduler {

    private static final String DATE_FORMAT = "yyyyMMdd";

    private final BidNoticeJobRunner bidNoticeJobRunner;
    private final DateRangeGenerator dateRangeGenerator;

    /**
     * ë§¤ì¼ ?ì •(0??0ë¶?0ì´????¤í–‰?˜ëŠ” ?¤ì?ì¤„ë§ ë©”ì„œ??1???„ë????´ì œê¹Œì???? ì§œë¥??”ë³„ë¡??˜ëˆ„???…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ?¤í–‰
     */
    @Scheduled(cron = "0 0 15 * * ?") // ë§¤ì¼ ?ì •???¤í–‰
//    @Scheduled(cron = "0 11 13 28 3 ?") // ?ŒìŠ¤?¸ìš©
    public void scheduleBidNoticeFetch() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ëŸ¬ ?œì‘ : {}", nowSeoul.toLocalDateTime());

        try {
            log.info("?•› ?…ì°°ê³µê³  ì´ˆê¸°???‘ì—… ?œì‘");
            bidNoticeJobRunner.runBidInitJob();
            log.info("?•› ?…ì°°ê³µê³  ì´ˆê¸°???‘ì—… ì¢…ë£Œ");

            // 1????? ì§œ?€ ?´ì œ ? ì§œ ê³„ì‚°
            LocalDate endDate = nowSeoul.toLocalDate().minusDays(1); // ?´ì œ
            LocalDate startDate = endDate.minusYears(1); // 1????>>>>>>> f5ab307d13eb94292dec8a65dd640396f5df93d6

            // ?”ë³„ ? ì§œ ë²”ìœ„ ?ì„± (YYYYMMDDhhmm ?•ì‹)
            List<DateRange> monthlyRanges = dateRangeGenerator.generateMonthlyRanges(startDate,
                endDate);

            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ?œì‘");
            // ?ì„±??ê°??”ë³„ ë²”ìœ„???€??ë°°ì¹˜ ?‘ì—… ?¤í–‰
            for (DateRange range : monthlyRanges) {
                String startDateStr = range.getStartDate();
                String endDateStr = range.getEndDate();

                log.debug("?•› ?…ì°°ê³µê³  ?”ë³„ ?˜ì§‘ ?‘ì—… ?œì‘: {} - {}", startDateStr, endDateStr);
                bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            }
            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ì¢…ë£Œ");

            log.info("?•› ?…ì°°ê³µê³  ?…ë°?´íŠ¸ ?‘ì—… ?œì‘");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("?•› ?…ì°°ê³µê³  ?…ë°?´íŠ¸ ?‘ì—… ì¢…ë£Œ");

            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ëŸ¬ ì¢…ë£Œ : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ë§ ì¤??¤ë¥˜ ë°œìƒ: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void scheduleBidNoticeFetch8AM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ëŸ¬ ?œì‘ : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ?œì‘");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(8).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ì¢…ë£Œ");

            log.info("?•› ?…ì°°ê³µê³  ?…ë°?´íŠ¸ ?‘ì—… ?œì‘");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("?•› ?…ì°°ê³µê³  ?…ë°?´íŠ¸ ?‘ì—… ì¢…ë£Œ");

            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ëŸ¬ ì¢…ë£Œ : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ë§ ì¤??¤ë¥˜ ë°œìƒ: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduleBidNoticeFetch11AM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ëŸ¬ ?œì‘ : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ?œì‘");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(11).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ì¢…ë£Œ");

            log.info("?•› ?…ì°°ê³µê³  ?…ë°?´íŠ¸ ?‘ì—… ?œì‘");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("?•› ?…ì°°ê³µê³  ?…ë°?´íŠ¸ ?‘ì—… ì¢…ë£Œ");

            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ëŸ¬ ì¢…ë£Œ : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ë§ ì¤??¤ë¥˜ ë°œìƒ: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 5 * * ?")
    public void scheduleBidNoticeFetch2PM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ëŸ¬ ?œì‘ : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ?œì‘");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(14).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ì¢…ë£Œ");

            log.info("?•› ?…ì°°ê³µê³  ?…ë°?´íŠ¸ ?‘ì—… ?œì‘");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("?•› ?…ì°°ê³µê³  ?…ë°?´íŠ¸ ?‘ì—… ì¢…ë£Œ");

            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ëŸ¬ ì¢…ë£Œ : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ë§ ì¤??¤ë¥˜ ë°œìƒ: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 7 * * ?")
    public void scheduleBidNoticeFetch4PM() {
        ZonedDateTime nowSeoul = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ëŸ¬ ?œì‘ : {}", nowSeoul.toLocalDateTime());
        try {
            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ?œì‘");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            String startDateStr = nowSeoul.withHour(0).withMinute(0).format(formatter);
            String endDateStr = nowSeoul.withHour(16).withMinute(0).format(formatter);

            bidNoticeJobRunner.runBidNoticeJobWithDateRange(startDateStr, endDateStr);
            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?‘ì—… ì¢…ë£Œ");

            log.info("?•› ?…ì°°ê³µê³  ?…ë°?´íŠ¸ ?‘ì—… ?œì‘");
            bidNoticeJobRunner.runBidUpdateJob();
            log.info("?•› ?…ì°°ê³µê³  ?…ë°?´íŠ¸ ?‘ì—… ì¢…ë£Œ");

            log.info("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ëŸ¬ ì¢…ë£Œ : {}", nowSeoul.toLocalDateTime());
        } catch (Exception e) {
            log.error("?•› ?…ì°°ê³µê³  ?˜ì§‘ ?¤ì?ì¤„ë§ ì¤??¤ë¥˜ ë°œìƒ: {}", e.getMessage(), e);
        }
    }

}
