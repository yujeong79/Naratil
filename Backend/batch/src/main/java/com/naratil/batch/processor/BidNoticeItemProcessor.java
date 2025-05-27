package com.naratil.batch.processor;

import com.naratil.batch.dto.nara.response.BidNoticeApiResponse.BidNoticeItem;
import com.naratil.batch.model.BidNotice;
import com.naratil.batch.service.SequenceGeneratorService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

/**
 * 입찰공고 API로 조회한 BidNoticeItem을 BidNotice 모델로 변환하는 클래스
 */
@Slf4j
public class BidNoticeItemProcessor implements ItemProcessor<BidNoticeItem, BidNotice> {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String bsnsDivNm;
    private final SequenceGeneratorService sequenceGenerator;
    private static final String SEQUENCE_NAME = "bids_sequence";

    public BidNoticeItemProcessor(SequenceGeneratorService sequenceGenerator, String bsnsDivNm) {
        this.sequenceGenerator = sequenceGenerator;
        this.bsnsDivNm = bsnsDivNm;
    }

    /**
     * API 응답 BidNoticeItem을 BidNotice 객체로 변환
     * @param item API에서 받아온 입찰공고 BidNoticeItem
     * @return 변환된 BidNotice 객체
     */
    @Override
    public BidNotice process(BidNoticeItem item) throws Exception {
        try {
            BidNotice bidNotice = new BidNotice();

            log.debug("?? 입찰 공고 조회 Process : {}-{}", item.getBidNtceNo(), item.getBidNtceOrd());

            bidNotice.setBidNtceId(sequenceGenerator.generateSequence(SEQUENCE_NAME));
            bidNotice.setNtceKindNm(item.getNtceKindNm());
            bidNotice.setBidNtceNo(item.getBidNtceNo());
            bidNotice.setBidNtceOrd(item.getBidNtceOrd());
            bidNotice.setBidNtceNm(item.getBidNtceNm());
            bidNotice.setBsnsDivNm(bsnsDivNm);
            bidNotice.setCntrctCnclsMthdNm(item.getCntrctCnclsMthdNm());
            bidNotice.setBidNtceDt(parseDateTimeOrNull(item.getBidNtceDt()));
            bidNotice.setBidBeginDt(parseDateTimeOrNull(item.getBidBeginDt()));
            bidNotice.setBidClseDt(parseDateTimeOrNull(item.getBidClseDt()));
            bidNotice.setOpengDt(parseDateTimeOrNull(item.getOpengDt()));
            // 참가제한지역
            // 업종명
            // 업종코드
            bidNotice.setNtceInsttNm(item.getNtceInsttNm());
            bidNotice.setDminsttNm(item.getDminsttNm());
            bidNotice.setPresmptPrce(parseLongOrNull(item.getPresmptPrce()));
            // 기초금액
            bidNotice.setPrearngPrceDcsnMthdNm(item.getPrearngPrceDcsnMthdNm());
            // 예가 변동폭
            bidNotice.setSucsfbidLwltRate(parseDoubleOrNull(item.getSucsfbidLwltRate()));
            bidNotice.setNtceInsttOfclNm(item.getNtceInsttOfclNm());
            bidNotice.setNtceInsttOfclTelNo(item.getNtceInsttOfclTelNo());
            bidNotice.setDcmtgOprtnDt(parseDateTimeOrNull(item.getDcmtgOprtnDt()));
            bidNotice.setDcmtgOprtnPlace(item.getDcmtgOprtnPlace());
            bidNotice.setBidNtceDtlUrl(item.getBidNtceDtlUrl());

            // 생성일시와 업데이트일시 설정
            LocalDateTime now = LocalDateTime.now();
            bidNotice.setCreatedAt(now);
            bidNotice.setUpdatedAt(now);

            return bidNotice;
        } catch (Exception e) {
            log.error("?? BidNotice 객체 변환 오류 : {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 문자열을 LocalDateTime 타입으로 변환
     * 실패시 null 반환
     */
    private LocalDateTime parseDateTimeOrNull(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            // 1. KST 湲곗??쇰줈 臾몄옄???뚯떛
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, dateTimeFormatter);

            // 2. KST ZonedDateTime ??UTC ZonedDateTime ??LocalDateTime
            ZonedDateTime kstZoned = localDateTime.atZone(ZoneId.of("Asia/Seoul"));
            ZonedDateTime utcZoned = kstZoned.withZoneSameInstant(ZoneOffset.UTC);

            // 3. UTC 湲곗? LocalDateTime 諛섑솚
            return utcZoned.toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.error("?? 날짜 시간 파싱 실패 : {}", e.getMessage());
            return null;
        }
    }

    /**
     * 문자열을 Long 타입으로 변환
     * 실패시 null 반환
     */
    private Long parseLongOrNull(String numStr) {
        if (numStr == null || numStr.trim().isEmpty()) {
            return null;
        }
        try {
            // 문자열을 Double로 먼저 파싱하여 소수점 처리
            double amount = Double.parseDouble(numStr);
            // 반올림하여 Long으로 변환
            return Math.round(amount);
        } catch (NumberFormatException e) {
            log.error("?? Long 파싱 실패 : {}", e.getMessage());
            return null;
        }
    }

    /**
     * 문자열을 Double 타입으로 변환
     * 실패시 null 반환
     */
    private Double parseDoubleOrNull(String numStr) {
        if (numStr == null || numStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            log.error("?? Double 파싱 실패 : {}", e.getMessage());
            return null;
        }
    }
}
