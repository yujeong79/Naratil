package com.naratil.batch.processor;

import com.naratil.batch.dto.nara.response.BasicPriceApiResponse.BasicPriceItem;
import com.naratil.batch.service.BidNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.StringUtils;

/**
 * 입찰공고 기초금액 API로 조회한 BasicPriceItem으로 MongoDB에 저장된 기존 데이터를 업데이트 하는 클래스
 */
@Slf4j
@RequiredArgsConstructor
public class BidBasicPriceItemProcessor implements ItemProcessor<BasicPriceItem, BasicPriceItem> {

    private final BidNoticeService bidNoticeService;

    /**
     * API 응답 BasicPriceItem의 공고번호-차수로 기초금액, 예가 변동폭을 업데이트
     * @param item API에서 받아온 기초금액 BasicPriceItem
     */
    @Override
    public BasicPriceItem process(BasicPriceItem item) throws Exception {
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

        // 데이터 업데이트
        boolean updated = bidNoticeService.updateBidBasicPrice(
            item.getBidNtceNo(),
            item.getBidNtceOrd(),
            bssamt,
            rsrvtnPrceRngBgnRate,
            rsrvtnPrceRngEndRate
        );

        if (updated) {
            log.debug("🐛 입찰공고 기초금액 정보 업데이트 성공: {}-{}", item.getBidNtceNo(), item.getBidNtceOrd());
            // 성공한 아이템만 Writer로 전달 (로깅 목적)
            return item;
        } else {
            log.warn("🐛 입찰공고 기초금액 정보 업데이트 실패: {}-{}", item.getBidNtceNo(), item.getBidNtceOrd());
            return null;
        }

    }

}
