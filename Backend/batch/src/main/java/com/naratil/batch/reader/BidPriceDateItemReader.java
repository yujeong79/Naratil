package com.naratil.batch.reader;

import com.naratil.batch.dto.nara.response.BasicPriceApiResponse;
import com.naratil.batch.dto.nara.response.BasicPriceApiResponse.BasicPriceItem;
import com.naratil.batch.service.BidNoticeService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

@Slf4j
public class BidPriceDateItemReader implements ItemReader<BasicPriceItem> {

    private final BidNoticeService bidNoticeService;
    private final String bsnsDivNm; // 업무 구분 (물품, 용역, 공사)
    private final String startDate; // 조회시작일시
    private final String endDate;   // 조회종료일시
    private static final int NUM_OF_ROWS = 100;    // 한 페이지 결과 수
    private final AtomicInteger currentPage = new AtomicInteger(1); // 현재 페이지 번호
    private Integer totalPage;  // 총 페이지 수
    private int currentItemIndex = 0;    // 현재 아이템 인덱스
    private List<BasicPriceItem> basicPriceItems = new ArrayList<>(); // 현재 페이지에서 처리 중인 아이템 목록

    public BidPriceDateItemReader(BidNoticeService bidNoticeService, String bsnsDivNm, String startDate, String endDate) {
        this.bidNoticeService = bidNoticeService;
        this.bsnsDivNm = bsnsDivNm;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public BasicPriceItem read()
        throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        // 초기 상태거나, 현재 페이지의 아이템을 모두 처리한 경우 새 페이지 요청
        if (basicPriceItems.isEmpty() || currentItemIndex >= basicPriceItems.size()) {
            // 모든 페이지를 처리한 경우 종료
            if (totalPage != null && currentPage.get() > totalPage) {
                log.debug("🐛 {} - {} 입찰공고 {} 기초금액 조회 Read 종료 : ", startDate, endDate, bsnsDivNm);
                return null;
            }

            // 다음 페이지 데이터 가져오기
            fetchNextPage();

            // 가져온 데이터가 없으면 종료
            if (basicPriceItems.isEmpty()) {
                log.debug("🐛 {} - {} 입찰공고 {} 기초금액 조회 Read 종료 : ", startDate, endDate, bsnsDivNm);
                return null;
            }

            // 새 페이지의 첫 아이템부터 읽기
            currentItemIndex = 0;
        }

        // 현재 인덱스의 아이템을 반환하고 인덱스 증가
        return basicPriceItems.get(currentItemIndex++);
    }

    private void fetchNextPage() {
        log.debug("🐛 {} - {} 입찰공고 {} 기초금액 {} 페이지 조회 요청", startDate, endDate, bsnsDivNm, currentPage.get());

        // API 호출하여 데이터 가져오기
        BasicPriceApiResponse basicPriceApiResponse = bidNoticeService.fetchBidPriceDate(bsnsDivNm, currentPage.get(), NUM_OF_ROWS, startDate, endDate);

        // 총 페이지 수 계산 (첫 호출에서만 계산)
        if (totalPage == null) {
            int totalCount = Integer.parseInt(basicPriceApiResponse.getResponse().getBody().getTotalCount());
            totalPage = (totalCount + NUM_OF_ROWS - 1) / NUM_OF_ROWS;
            log.debug("🐛 총 페이지: {}, 총 데이터 {}", totalPage, totalCount);
        }

        // 아이템 목록
        if (basicPriceApiResponse != null &&
            basicPriceApiResponse.getResponse() != null &&
            basicPriceApiResponse.getResponse().getBody() != null) {
            basicPriceItems = basicPriceApiResponse.getResponse().getBody().getItems();

            // 아이템 목록이 null일 경우 빈 리스트로 초기화
            if (basicPriceItems == null) {
                basicPriceItems = new ArrayList<>();
            }
        } else {
            basicPriceItems = new ArrayList<>();
            log.warn("🐛 {} - {} 입찰공고 {} 기초금액 {} 페이지 조회 비정상적인 응답", startDate, endDate, bsnsDivNm, currentPage.get());
        }

        // 페이지 번호 증가
        currentPage.incrementAndGet();
    }
}
