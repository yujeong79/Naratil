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
 * �������� API�� ��ȸ�� BidNoticeItem�� BidNotice �𵨷� ��ȯ�ϴ� Ŭ����
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
     * API ���� BidNoticeItem�� BidNotice ��ü�� ��ȯ
     * @param item API���� �޾ƿ� �������� BidNoticeItem
     * @return ��ȯ�� BidNotice ��ü
     */
    @Override
    public BidNotice process(BidNoticeItem item) throws Exception {
        try {
            BidNotice bidNotice = new BidNotice();

            log.debug("?? ���� ���� ��ȸ Process : {}-{}", item.getBidNtceNo(), item.getBidNtceOrd());

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
            // ������������
            // ������
            // �����ڵ�
            bidNotice.setNtceInsttNm(item.getNtceInsttNm());
            bidNotice.setDminsttNm(item.getDminsttNm());
            bidNotice.setPresmptPrce(parseLongOrNull(item.getPresmptPrce()));
            // ���ʱݾ�
            bidNotice.setPrearngPrceDcsnMthdNm(item.getPrearngPrceDcsnMthdNm());
            // ���� ������
            bidNotice.setSucsfbidLwltRate(parseDoubleOrNull(item.getSucsfbidLwltRate()));
            bidNotice.setNtceInsttOfclNm(item.getNtceInsttOfclNm());
            bidNotice.setNtceInsttOfclTelNo(item.getNtceInsttOfclTelNo());
            bidNotice.setDcmtgOprtnDt(parseDateTimeOrNull(item.getDcmtgOprtnDt()));
            bidNotice.setDcmtgOprtnPlace(item.getDcmtgOprtnPlace());
            bidNotice.setBidNtceDtlUrl(item.getBidNtceDtlUrl());

            // �����Ͻÿ� ������Ʈ�Ͻ� ����
            LocalDateTime now = LocalDateTime.now();
            bidNotice.setCreatedAt(now);
            bidNotice.setUpdatedAt(now);

            return bidNotice;
        } catch (Exception e) {
            log.error("?? BidNotice ��ü ��ȯ ���� : {}", e.getMessage());
            throw e;
        }
    }

    /**
     * ���ڿ��� LocalDateTime Ÿ������ ��ȯ
     * ���н� null ��ȯ
     */
    private LocalDateTime parseDateTimeOrNull(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            // 1. KST 기�??�로 문자???�싱
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, dateTimeFormatter);

            // 2. KST ZonedDateTime ??UTC ZonedDateTime ??LocalDateTime
            ZonedDateTime kstZoned = localDateTime.atZone(ZoneId.of("Asia/Seoul"));
            ZonedDateTime utcZoned = kstZoned.withZoneSameInstant(ZoneOffset.UTC);

            // 3. UTC 기�? LocalDateTime 반환
            return utcZoned.toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.error("?? ��¥ �ð� �Ľ� ���� : {}", e.getMessage());
            return null;
        }
    }

    /**
     * ���ڿ��� Long Ÿ������ ��ȯ
     * ���н� null ��ȯ
     */
    private Long parseLongOrNull(String numStr) {
        if (numStr == null || numStr.trim().isEmpty()) {
            return null;
        }
        try {
            // ���ڿ��� Double�� ���� �Ľ��Ͽ� �Ҽ��� ó��
            double amount = Double.parseDouble(numStr);
            // �ݿø��Ͽ� Long���� ��ȯ
            return Math.round(amount);
        } catch (NumberFormatException e) {
            log.error("?? Long �Ľ� ���� : {}", e.getMessage());
            return null;
        }
    }

    /**
     * ���ڿ��� Double Ÿ������ ��ȯ
     * ���н� null ��ȯ
     */
    private Double parseDoubleOrNull(String numStr) {
        if (numStr == null || numStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            log.error("?? Double �Ľ� ���� : {}", e.getMessage());
            return null;
        }
    }
}
