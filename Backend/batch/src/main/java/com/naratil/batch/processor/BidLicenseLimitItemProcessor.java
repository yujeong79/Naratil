package com.naratil.batch.processor;

import com.naratil.batch.dto.nara.response.LicenseLimitApiResponse.LicenseLimitItem;
import com.naratil.batch.service.BidNoticeService;
import com.naratil.batch.util.NumberUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class BidLicenseLimitItemProcessor implements ItemProcessor<LicenseLimitItem, LicenseLimitItem> {

    private final BidNoticeService bidNoticeService;

    @Override
    public LicenseLimitItem process(LicenseLimitItem item) throws Exception {
        // 첫번째 면허 정보만 수집
        if (!StringUtils.hasText(item.getLmtSno()) || !item.getLmtSno().equals("1")) {
            return null;
        }

        // 필수 식별 정보 확인
        if (!StringUtils.hasText(item.getBidNtceNo()) || !StringUtils.hasText(item.getBidNtceOrd())) {
            log.warn("🐛 입찰공고 번호 또는 차수 정보가 누락되었습니다: {}", item);
            return null;
        }

        // 면허제한명에서 업종코드, 업종명 분리
        String lcnsLmtNm = item.getLcnsLmtNm();
        String indstrytyCd; // 업종코드
        String indstrytyNm; // 업종명
        if (lcnsLmtNm != null && lcnsLmtNm.contains("/")) {
            String[] parts = lcnsLmtNm.split("/", 2);
            if (parts.length == 2) {
                indstrytyCd = parts[1].trim();
                indstrytyNm = parts[0].trim();
            } else {
                log.warn("🐛 잘못된 면허정보: {}", lcnsLmtNm);
                return null;
            }
        } else {
            log.warn("🐛 잘못된 면허정보: {}", lcnsLmtNm);
            return null;
        }

        // 데이터 업데이트
        boolean updated = bidNoticeService.updateBidLicenseLimit(
            item.getBidNtceNo(),
            item.getBidNtceOrd(),
            indstrytyCd,
            indstrytyNm
        );

        if (updated) {
            log.debug("🐛 입찰공고 면허제한정보 업데이트 성공: {}-{}", item.getBidNtceNo(), item.getBidNtceOrd());
            // 성공한 아이템만 Writer로 전달 (로깅 목적)
            return item;
        } else {
            log.warn("🐛 입찰공고 면허제한정보 업데이트 실패: {}-{}", item.getBidNtceNo(), item.getBidNtceOrd());
            return null;
        }
    }
}
