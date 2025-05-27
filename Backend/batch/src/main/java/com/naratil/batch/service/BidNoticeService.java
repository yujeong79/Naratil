package com.naratil.batch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.naratil.batch.dto.nara.BidNoticeId;
import com.naratil.batch.dto.nara.BidUpdateItem;
import com.naratil.batch.dto.nara.response.BasicPriceApiResponse;
import com.naratil.batch.dto.nara.response.BidNoticeApiResponse;
import com.naratil.batch.dto.nara.response.LicenseLimitApiResponse;
import com.naratil.batch.dto.nara.response.RegionLimitApiResponse;
import com.naratil.batch.model.BidNotice;
import com.naratil.batch.util.BidNoticeApiUtil;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 입찰공고 데이터를 공공데이터 포털 API로부터 조회하는 서비스 클래스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BidNoticeService {

    private final BidNoticeApiUtil bidNoticeApiUtil;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;

    public BidNoticeApiResponse fetchBidNoticeDate(String bsnsDivNm, int pageNo, int numOfRows, String startDate, String endDate) {
        log.debug("🐛 {} - {} 입찰공고 {} {} 페이지 조회 준비", bsnsDivNm, startDate, endDate, pageNo);

        URI uri = bidNoticeApiUtil.buildBidNoticeDateApiUri(bsnsDivNm, pageNo, numOfRows, startDate, endDate);

        try {
            // 공공데이터 API 호출
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            // HTTP 응답 확인
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("🐛 API 호출 실패, 상태 코드: {}", response.getStatusCode());
                return null;
            }
            // JSON 문자열을 BidNoticeApiResponse 객체로 변환
            BidNoticeApiResponse bidNoticeApiResponse = objectMapper.readValue(response.getBody(), BidNoticeApiResponse.class);
            log.debug("🐛 {} - {} 입찰공고 {} {} 페이지 조회 성공", bsnsDivNm, startDate, endDate, pageNo);
            return bidNoticeApiResponse;
        } catch (Exception e) {
            log.error("🐛 {} - {} 입찰공고 {} {} 페이지 조회 실패 : {}", bsnsDivNm, startDate, endDate, pageNo, e.getMessage());
            return null;
        }
    }

    public BasicPriceApiResponse fetchBidPriceDate(String bsnsDivNm, int pageNo, int numOfRows, String startDate, String endDate) {
        log.debug("🐛 {} - {} 입찰공고 {} 기초금액 {} 페이지 조회 준비", startDate, endDate, bsnsDivNm, pageNo);

        URI uri = bidNoticeApiUtil.buildBidPriceDateApiUri(bsnsDivNm, pageNo, numOfRows, startDate, endDate);
        try {
            // 공공데이터 API 호출
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            // HTTP 응답 확인
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("🐛 API 호출 실패, 상태 코드: {}", response.getStatusCode());
                return null;
            }

            // JSON 문자열을 BasicPriceApiResponse 객체로 변환
            BasicPriceApiResponse bidPriceApiResponse = objectMapper.readValue(response.getBody(), BasicPriceApiResponse.class);
            log.debug("🐛 {} - {} 입찰공고 {} 기초금액 {} 페이지 조회 성공", startDate, endDate, bsnsDivNm, pageNo);
            return bidPriceApiResponse;
        } catch (Exception e) {
            log.error("🐛 {} - {} 입찰공고 {} 기초금액 {} 페이지 조회 실패 : {}", startDate, endDate, bsnsDivNm, pageNo, e.getMessage());
            return null;
        }
    }

    // fetchBidLicenseDate

    // fetchBidRegionDate


    // fetchBidNoticeNo
    public BasicPriceApiResponse fetchBidPriceNo(String bsnsDivNm, int pageNo, int numOfRows, String bidNtceNo, String bidNtceOrd) {
        log.debug("🐛 입찰공고 {} 기초금액 {}-{} 조회 준비", bsnsDivNm, bidNtceNo, bidNtceOrd);

        URI uri = bidNoticeApiUtil.buildBidPriceNoApiUri(bsnsDivNm, pageNo, numOfRows, bidNtceNo, bidNtceOrd);
        try {
            // 공공데이터 API 호출
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            // HTTP 응답 확인
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("🐛 API 호출 실패, 상태 코드: {}", response.getStatusCode());
                return null;
            }

            // JSON 문자열을 BasicPriceApiResponse 객체로 변환
            BasicPriceApiResponse bidPriceApiResponse = objectMapper.readValue(response.getBody(), BasicPriceApiResponse.class);
            log.debug("🐛 입찰공고 {} 기초금액 {}-{} 조회 성공", bsnsDivNm, bidNtceNo, bidNtceOrd);
            return bidPriceApiResponse;
        } catch (Exception e) {
            log.error("🐛 입찰공고 {} 기초금액 {}-{} 조회 실패 : {}", bsnsDivNm, bidNtceNo, bidNtceOrd, e.getMessage());
            return null;
        }
    }

    public LicenseLimitApiResponse fetchBidLicenseNo(int pageNo, int numOfRows, String bidNtceNo, String bidNtceOrd) {
        URI uri = bidNoticeApiUtil.buildBidLicenseNoApiUri(pageNo, numOfRows, bidNtceNo, bidNtceOrd);

        try {
            // 공공데이터 API 호출
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            // HTTP 응답 확인
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("🐛 API 호출 실패, 상태 코드: {}", response.getStatusCode());
                return null;
            }

            // JSON 문자열을 LicenseLimitApiResponse 객체로 변환
            return objectMapper.readValue(response.getBody(), LicenseLimitApiResponse.class);
        } catch (Exception e) {
            log.error("🐛 입찰공고 면허제한정보 조회 실패 {}-{} : {}", bidNtceNo, bidNtceOrd, e.getMessage());
            return null;
        }
    }

    public RegionLimitApiResponse fetchBidRegionNo(int pageNo, int numOfRows, String bidNtceNo, String bidNtceOrd) {

        URI uri = bidNoticeApiUtil.buildBidRegionNoApiUri(pageNo, numOfRows, bidNtceNo, bidNtceOrd);

        try {
            // 공공데이터 API 호출
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            // HTTP 응답 확인
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("🐛 API 호출 실패, 상태 코드: {}", response.getStatusCode());

                return null;
            }

            // JSON 문자열을 LicenseLimitApiResponse 객체로 변환
            RegionLimitApiResponse regionLimitApiResponse = objectMapper.readValue(response.getBody(), RegionLimitApiResponse.class);
            return regionLimitApiResponse;
        } catch (Exception e) {
            log.error("🐛 입찰공고 참가가능지역정보 조회 실패 {}-{} : {}", bidNtceNo, bidNtceOrd, e.getMessage());
            return null;
        }
    }

    public boolean updateBidNo(BidUpdateItem item) {
        // 쿼리 조건 설정
        Query query = new Query(Criteria.where("bidNtceNo").is(item.getBidNtceNo()).and("bidNtceOrd").is(item.getBidNtceOrd()));

        // 업데이트 내용 설정
        Update update = new Update()
            .set("bssamt", item.getBssamt())
            .set("rsrvtnPrceRngBgnRate", item.getRsrvtnPrceRngBgnRate())
            .set("rsrvtnPrceRngEndRate", item.getRsrvtnPrceRngEndRate())
            .set("indstrytyCd", item.getIndstrytyCd())
            .set("indstrytyNm", item.getIndstrytyNm())
            .set("prtcptPsblRgnNm", item.getPrtcptPsblRgnNm());

        // 조건에 맞는 첫 번째 문서만 업데이트 (새 문서 생성 안 함)
        UpdateResult result = mongoTemplate.updateFirst(query, update, BidNotice.class);

        // 업데이트된 문서가 있는지 확인
        return result.getModifiedCount() > 0;
    }

    public boolean updateBidBasicPrice(String bidNtceNo, String bidNtceOrd, Long bssamt, Double rsrvtnPrceRngBgnRate, Double rsrvtnPrceRngEndRate) {
        // 쿼리 조건 설정
        Query query = new Query(Criteria.where("bidNtceNo").is(bidNtceNo).and("bidNtceOrd").is(bidNtceOrd));

        // 업데이트 내용 설정
        Update update = new Update()
            .set("bssamt", bssamt)
            .set("rsrvtnPrceRngBgnRate", rsrvtnPrceRngBgnRate)
            .set("rsrvtnPrceRngEndRate", rsrvtnPrceRngEndRate);

        // 조건에 맞는 첫 번째 문서만 업데이트 (새 문서 생성 안 함)
        UpdateResult result = mongoTemplate.updateFirst(query, update, BidNotice.class);

        // 업데이트된 문서가 있는지 확인
        return result.getMatchedCount() > 0;
    }

    public boolean updateBidLicenseLimit(String bidNtceNo, String bidNtceOrd, String indstrytyCd, String indstrytyNm) {
        // 쿼리 조건 설정
        Query query = new Query(Criteria.where("bidNtceNo").is(bidNtceNo).and("bidNtceOrd").is(bidNtceOrd));

        // 업데이트 내용 설정
        Update update = new Update()
            .set("indstrytyCd", indstrytyCd)
            .set("indstrytyNm", indstrytyNm);

        // 조건에 맞는 첫 번째 문서만 업데이트 (새 문서 생성 안 함)
        UpdateResult result = mongoTemplate.updateFirst(query, update, BidNotice.class);

        // 업데이트된 문서가 있는지 확인
        return result.getMatchedCount() > 0;
    }

    public boolean updateBidRegionLimit(String bidNtceNo, String bidNtceOrd, String prtcptPsblRgnNm) {
        // 쿼리 조건 설정
        Query query = new Query(Criteria.where("bidNtceNo").is(bidNtceNo).and("bidNtceOrd").is(bidNtceOrd)) ;

        // 업데이트 내용 설정
        Update update = new Update().set("prtcptPsblRgnNm", prtcptPsblRgnNm);

        // 조건에 맞는 첫 번째 문서만 업데이트 (새 문서 생성 안 함)
        UpdateResult result = mongoTemplate.updateFirst(query, update, BidNotice.class);

        // 업데이트된 문서가 있는지 확인
        return result.getMatchedCount() > 0;
    }

    public long updateNotUpdatedBid() {
        // 업데이트 여부 필드가 false이거나 존재하지 않는 문서 조회
        Query query = new Query(new Criteria().orOperator(
            Criteria.where("isUpdated").is(false),
            Criteria.where("isUpdated").exists(false)
        ));

        Update update = new Update().set("isUpdated", true).set("updatedAt", LocalDateTime.now());

        UpdateResult result = mongoTemplate.updateMulti(query, update, BidNotice.class);

        return result.getModifiedCount();
    }
    public List<BidNoticeId> findNotUpdatedBidId() {
        // 업데이트 여부 필드가 false이거나 존재하지 않는 문서 조회
        Criteria criteria = new Criteria().orOperator(
            Criteria.where("isUpdated").is(false),
            Criteria.where("isUpdated").exists(false)
        );

        // 업데이트되지 않은 공고의 bidNtceNo, bidNtceOrd 조회
        Query query = new Query();
        query.addCriteria(criteria);
        query.fields().include("bidNtceNo").include("bidNtceOrd").include("bsnsDivNm");

        List<BidNoticeId> result = mongoTemplate.find(query, BidNoticeId.class, "bids");

        return result;
    }

    public void cleanupDuplicateBid() {
        // 모든 고유한 bidNtceNo 목록 조회
        List<String> distinctBidNtceNos = mongoTemplate.findDistinct(
            new Query(), "bidNtceNo", BidNotice.class, String.class);

        // bidNtceNo를 하나씩 확인하면서 최신 bidNtceOrd인 공고만 남기고 삭제
        for (String bidNtceNo : distinctBidNtceNos) {
            removeDuplicateBidForBidNtceNo(bidNtceNo);
        }
    }

    private void removeDuplicateBidForBidNtceNo(String bidNtceNo) {
        // 공고번호가 bidNtceNo인 모든 공고 조회
        Query query = new Query(Criteria.where("bidNtceNo").is(bidNtceNo));
        List<BidNotice> bids = mongoTemplate.find(query, BidNotice.class);

        // 중복 없음
        if (bids.size() <= 1) {
            return;
        }

        // 가장 높은 bidNtceOrd 찾기
        Optional<BidNotice> latestBid = bids.stream().max(Comparator.comparing(BidNotice::getBidNtceOrd));

        // 최신 공고를 제외한 나머지 삭제
        if (latestBid.isPresent()) {
            // bidNtceNo와 일치하는 공고중에 latestBid 공고를 제외한 공고 조회
            Query deleteQuery = new Query(Criteria.where("bidNtceNo").is(bidNtceNo)
                .and("bidNtceId").ne(latestBid.get().getBidNtceId()));
            mongoTemplate.remove(deleteQuery, BidNotice.class);
        }
    }

    // bids 컬렉션 초기화
    // drop -> clear로 이름 변경 필요
    public void dropBidsCollection() {
        try {
            mongoTemplate.remove(new Query(), "bids");

            // Auto-increment 시퀀스 리셋
            Query query = new Query(Criteria.where("_id").is("bids_sequence"));
            Update update = new Update().set("seq", 0);
            mongoTemplate.upsert(query, update, "database_sequences");
        } catch (Exception e) {
            log.error("🐛 입찰공고 컬렉션 초기화 실패 : {}", e.getMessage());
        }
    }
}
