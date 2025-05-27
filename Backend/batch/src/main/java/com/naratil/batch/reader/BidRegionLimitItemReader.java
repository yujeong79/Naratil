package com.naratil.batch.reader;

import com.naratil.batch.dto.nara.BidNoticeId;
import com.naratil.batch.dto.nara.response.RegionLimitApiResponse;
import com.naratil.batch.dto.nara.response.RegionLimitApiResponse.RegionLimitItem;
import com.naratil.batch.service.BidNoticeService;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

@Slf4j
public class BidRegionLimitItemReader implements ItemReader<RegionLimitItem> {
    private final BidNoticeService bidNoticeService;
    private static final int PAGE_NO = 1;   // 페이지번호
    private static final int NUM_OF_ROWS = 1;    // 한 페이지 결과 수
    private List<BidNoticeId> bidNoticeIds; // 공고번호와 차수를 보관할 리스트
    private Iterator<BidNoticeId> bidNoticeIterator; // 공고번호와 차수를 순회할 iterator
    private BidNoticeId currentBidNotice; // 현재 처리 중인 공고

    public BidRegionLimitItemReader(BidNoticeService bidNoticeService) {
        this.bidNoticeService = bidNoticeService;
    }

    @Override
    public RegionLimitItem read()
        throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        // 초기화 : MongoDB에서 입찰공고 정보 가져오기
        if (bidNoticeIds == null) {
            bidNoticeIds = bidNoticeService.findNotUpdatedBidId();
            bidNoticeIterator = bidNoticeIds.iterator();
            log.debug("🐛 {} 개의 입찰공고에 대한 참가가능지역정보 조회 시작", bidNoticeIds.size());
        }

        // 모든 bidNoticeIds를 처리할 때 까지 반복
        if(!bidNoticeIterator.hasNext()) {
            log.debug("🐛 {} 개의 입찰공고에 대한 참가가능지역정보 조회 완료", bidNoticeIds.size());
            return null;
        }

        // API 호출하여 다음 공고 데이터 가져오기
        currentBidNotice = bidNoticeIterator.next();
        RegionLimitApiResponse regionLimitApiResponse = bidNoticeService.fetchBidRegionNo(PAGE_NO, NUM_OF_ROWS, currentBidNotice.getBidNtceNo(), currentBidNotice.getBidNtceOrd());

        // 비정상적인 응답
        if (regionLimitApiResponse == null ||
            regionLimitApiResponse.getResponse() == null ||
            regionLimitApiResponse.getResponse().getBody() == null) {
            log.warn("🐛 입찰 공고 참가가능지역정보 조회 비정상적인 응답 {}", currentBidNotice.toString());
            return read();  // 다음 공고로 넘어감
        }
        // 면허제한정보 없음
        else if (regionLimitApiResponse.getResponse().getBody().getItems() == null ||
            regionLimitApiResponse.getResponse().getBody().getItems().isEmpty()) {
            log.debug("🐛 입찰 공고 참가가능지역정보 조회 면허제한없음 {} ", currentBidNotice.toString());
            return read();  // 다음 공고로 넘어감
        }
        // 정상적인 정보가 있는 경우 첫번째 아이템 반환
        else {
            RegionLimitItem item = regionLimitApiResponse.getResponse().getBody().getItems().get(0);
            log.debug("🐛 입찰 공고 참가가능지역정보 조회 성공 {} ", currentBidNotice.toString());
            return item;
        }

    }

}
