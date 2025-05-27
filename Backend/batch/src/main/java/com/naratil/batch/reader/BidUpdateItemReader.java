package com.naratil.batch.reader;

import com.naratil.batch.dto.nara.BidNoticeId;
import com.naratil.batch.dto.nara.BidUpdateItem;
import com.naratil.batch.dto.nara.response.BasicPriceApiResponse;
import com.naratil.batch.dto.nara.response.BasicPriceApiResponse.BasicPriceItem;
import com.naratil.batch.dto.nara.response.LicenseLimitApiResponse;
import com.naratil.batch.dto.nara.response.RegionLimitApiResponse;
import com.naratil.batch.service.BidNoticeService;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

@Slf4j
public class BidUpdateItemReader implements ItemReader<BidUpdateItem> {

    private final BidNoticeService bidNoticeService;
    private static final int PAGE_NO = 1;   // 페이지번호
    private static final int NUM_OF_ROWS = 1;    // 한 페이지 결과 수
    private List<BidNoticeId> bidNoticeIds; // 공고번호와 차수를 보관할 리스트
    private Iterator<BidNoticeId> bidNoticeIterator; // 공고번호와 차수를 순회할 iterator
    private BidNoticeId currentBidNotice; // 현재 처리 중인 공고

    public BidUpdateItemReader(BidNoticeService bidNoticeService) {
        this.bidNoticeService = bidNoticeService;
    }

    @Override
    public BidUpdateItem read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        // 초기화 : MongoDB에서 입찰공고 정보 가져오기
        if (bidNoticeIds == null) {
            bidNoticeIds = bidNoticeService.findNotUpdatedBidId();
            bidNoticeIterator = bidNoticeIds.iterator();
            log.debug("🐛 {} 개의 입찰공고에 대한 수정 시작", bidNoticeIds.size());
        }

        // 모든 bidNoticeIds를 처리할 때 까지 반복
        if(!bidNoticeIterator.hasNext()) {
            log.debug("🐛 {} 개의 입찰공고에 대한 수정 완료", bidNoticeIds.size());
            return null;
        }

        // API 호출하여 다음 공고 데이터 가져오기
        currentBidNotice = bidNoticeIterator.next();
        BasicPriceApiResponse basicPriceApiResponse = bidNoticeService.fetchBidPriceNo(
            currentBidNotice.getBsnsDivNm(), PAGE_NO, NUM_OF_ROWS, currentBidNotice.getBidNtceNo(), currentBidNotice.getBidNtceOrd());
        LicenseLimitApiResponse licenseLimitApiResponse = bidNoticeService.fetchBidLicenseNo(
            PAGE_NO, NUM_OF_ROWS, currentBidNotice.getBidNtceNo(), currentBidNotice.getBidNtceOrd());
        RegionLimitApiResponse regionLimitApiResponse = bidNoticeService.fetchBidRegionNo(
            PAGE_NO, NUM_OF_ROWS, currentBidNotice.getBidNtceNo(), currentBidNotice.getBidNtceOrd());

        BidUpdateItem item = new BidUpdateItem();
        item.setBidNtceNo(currentBidNotice.getBidNtceNo());
        item.setBidNtceOrd(currentBidNotice.getBidNtceOrd());

        if (basicPriceApiResponse != null &&
            basicPriceApiResponse.getResponse() != null &&
            basicPriceApiResponse.getResponse().getBody() != null &&
            basicPriceApiResponse.getResponse().getBody().getItems() != null &&
            !basicPriceApiResponse.getResponse().getBody().getItems().isEmpty()) {

            BasicPriceItem basicPriceItem = basicPriceApiResponse.getResponse().getBody().getItems().get(0);
            item.setBssamt(basicPriceItem.getBssamt());
            item.setRsrvtnPrceRngBgnRate(basicPriceItem.getRsrvtnPrceRngBgnRate());
            item.setRsrvtnPrceRngEndRate(basicPriceItem.getRsrvtnPrceRngEndRate());

            log.debug("🐛 입찰공고 기초금액 조회 성공 {} ", currentBidNotice.toString());
        }

        if (licenseLimitApiResponse != null &&
            licenseLimitApiResponse.getResponse() != null &&
            licenseLimitApiResponse.getResponse().getBody() != null &&
            licenseLimitApiResponse.getResponse().getBody().getItems() != null &&
            !licenseLimitApiResponse.getResponse().getBody().getItems().isEmpty()) {

            item.setLcnsLmtNm(licenseLimitApiResponse.getResponse().getBody().getItems().get(0).getLcnsLmtNm());
            log.debug("🐛 입찰공고 면허제한정보 조회 성공 {} ", currentBidNotice.toString());
        }

        if (regionLimitApiResponse != null &&
            regionLimitApiResponse.getResponse() != null &&
            regionLimitApiResponse.getResponse().getBody() != null &&
            regionLimitApiResponse.getResponse().getBody().getItems() != null &&
            !regionLimitApiResponse.getResponse().getBody().getItems().isEmpty()) {

            item.setPrtcptPsblRgnNm(regionLimitApiResponse.getResponse().getBody().getItems().get(0).getPrtcptPsblRgnNm());
            log.debug("🐛 입찰공고 참가가능지역정보 조회 성공 {} ", currentBidNotice.toString());
        }

        return item;
    }
}
