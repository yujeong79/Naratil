package com.naratil.batch.processor;

import com.naratil.batch.dto.nara.response.RegionLimitApiResponse.RegionLimitItem;
import com.naratil.batch.service.BidNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.StringUtils;

/**
 * 입찰공고 참가가능지역정보 조회 API 응답 데이터로 MongoDB에 저장된 기존 데이터를 업데이트 하는 클래스
 */
@Slf4j
@RequiredArgsConstructor
public class BidRegionLimitItemProcessor implements ItemProcessor<RegionLimitItem, RegionLimitItem> {

    private final BidNoticeService bidNoticeService;

    /**
     * API 응답 RegionLimitItem의 공고번호-차수로 참가가능지역명을 업데이트
     * @param item API에서 받아온 물품 기초금액 RegionLimitItem
     */
    @Override
    public RegionLimitItem process(RegionLimitItem item) throws Exception {
        // 필수 식별 정보 확인
        if (!StringUtils.hasText(item.getBidNtceNo()) || !StringUtils.hasText(item.getBidNtceOrd())) {
            log.warn("🐛 입찰공고 번호 또는 차수 정보가 누락되었습니다: {}", item);
            return null;
        }

        // 데이터 업데이트
        boolean updated = bidNoticeService.updateBidRegionLimit(
            item.getBidNtceNo(),
            item.getBidNtceOrd(),
            item.getPrtcptPsblRgnNm()
        );

        if (updated) {
            log.debug("🐛 입찰공고 참가가능지역정보 업데이트 성공: {}-{}", item.getBidNtceNo(), item.getBidNtceOrd());
            // 성공한 아이템만 Writer로 전달 (로깅 목적)
            return item;
        } else {
            log.warn("🐛 입찰공고 참가가능지역정보 업데이트 실패: {}-{}", item.getBidNtceNo(), item.getBidNtceOrd());
            return null;
        }
    }
}
