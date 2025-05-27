package com.naratil.batch.processor;

import com.naratil.batch.dto.nara.BidUpdateItem;
import com.naratil.batch.service.BidNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class BidUpdateItemProcessor implements ItemProcessor<BidUpdateItem, BidUpdateItem> {

    private final BidNoticeService bidNoticeService;

    private static String indstrytyCd; // 업종코드
    private static String indstrytyNm; // 업종명
    @Override
    public BidUpdateItem process(BidUpdateItem item) throws Exception {
        // 필수 식별 정보 확인
        if (!StringUtils.hasText(item.getBidNtceNo()) || !StringUtils.hasText(item.getBidNtceOrd())) {
            log.warn("🐛 입찰공고 번호 또는 차수 정보가 누락되었습니다: {}", item);
            return null;
        }

        // 데이터 파싱 및 변환
        Long bssamt = null;
        Double rsrvtnPrceRngBgnRate = null;
        Double rsrvtnPrceRngEndRate = null;

        // 기초금액 파싱
        if (StringUtils.hasText(item.getBssamt())) {
            try {
                // 문자열을 Double로 먼저 파싱
                double amount = Double.parseDouble(item.getBssamt());
                // 반올림하여 Long으로 변환
                bssamt = Math.round(amount);
            } catch (NumberFormatException e) {
                log.warn("🐛 기초금액 형식 오류: {}", item.getBssamt());
            }
        }

        // 예가 변동폭 시작률 파싱
        if (StringUtils.hasText(item.getRsrvtnPrceRngBgnRate())) {
            try {
                rsrvtnPrceRngBgnRate = Double.parseDouble(item.getRsrvtnPrceRngBgnRate());
            } catch (NumberFormatException e) {
                log.warn("🐛 예가 변동폭 시작률 형식 오류: {}", item.getRsrvtnPrceRngBgnRate());
            }
        }

        // 예가 변동폭 종료률 파싱
        if (StringUtils.hasText(item.getRsrvtnPrceRngEndRate())) {
            try {
                rsrvtnPrceRngEndRate = Double.parseDouble(item.getRsrvtnPrceRngEndRate());
            } catch (NumberFormatException e) {
                log.warn("🐛 예가 변동폭 종료률 형식 오류: {}", item.getRsrvtnPrceRngEndRate());
            }
        }

        // 면허제한명에서 업종코드, 업종명 분리
        String lcnsLmtNm = item.getLcnsLmtNm();
        String indstrytyCd; // 업종코드
        String indstrytyNm; // 업종명
        if (lcnsLmtNm != null && lcnsLmtNm.contains("/")) {
            String[] parts = lcnsLmtNm.split("/", 2);
            if (parts.length == 2) {
                item.setIndstrytyCd(parts[1].trim());
                item.setIndstrytyNm(parts[0].trim());
            } else {
                log.warn("🐛 잘못된 면허정보: {}", lcnsLmtNm);
            }
        } else {
            log.warn("🐛 잘못된 면허정보: {}", lcnsLmtNm);
        }

        boolean updated = bidNoticeService.updateBidNo(item);
        if (updated) {
            log.info("🐛 입찰공고 정보 업데이트 성공: {}-{}", item.getBidNtceNo(), item.getBidNtceOrd());
            // 성공한 아이템만 Writer로 전달 (로깅 목적)
            return item;
        }else {
            log.warn("🐛 입찰공고 정보 업데이트 실패: {}-{}", item.getBidNtceNo(), item.getBidNtceOrd());
            return null;
        }
    }
}
