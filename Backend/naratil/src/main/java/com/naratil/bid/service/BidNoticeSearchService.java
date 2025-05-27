package com.naratil.bid.service;

import com.naratil.bid.dto.BidSearchRequestDto;
import com.naratil.bid.entity.BidNoticeMongoDB;
import com.naratil.industry.repository.IndustryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidNoticeSearchService {

    private final MongoTemplate mongoTemplate; // MongoTemplate 주입
    private final IndustryRepository industryRepository;

    // 메인 검색 메서드
    public List<Document> searchBids(BidSearchRequestDto requestDto) {
        log.debug("searchBids");
        Query query = new Query();

        // 각 필터 조건을 메서드로 모듈화하여 추가
        applyBidNumFilter(requestDto, query);       // 공고 번호                완료
        applyBidNameFilter(requestDto, query);      // 공고 이름                완료
        applyTaskTypeFilter(requestDto, query);     // 업무 구분                완료
        applyIndustryFilter(requestDto, query);     // 업종                    완료
        applyAgencyChoiceFilter(requestDto, query); // 기관                    완료
        applyBudgetFilter(requestDto, query);       // 금액 완료                완료
        applyDateFilter(requestDto, query);         // 입찰날짜(YYYY-MM-DD)     완료
        applyRegionFilter(requestDto, query);       // 지역                    완료
        applySorting(requestDto, query);            // 정렬                    완료

        // MongoTemplate을 통해 쿼리 실행
        return findDocumentsBySearchOption(query);
    }

    // 공고 번호 조건을 추가하는 메서드
    private void applyBidNumFilter(BidSearchRequestDto requestDto, Query query) {
        if (requestDto.bidNum() != null && !requestDto.bidNum().isEmpty()) {  // null과 "" 이 아니라면
            Criteria bidNumCriteria = new Criteria().orOperator(
                    Criteria.where("bidNtceNo").regex(requestDto.bidNum(), "i")   // 공고번호 검색 (대소문자 무시)
            );
            log.debug("검색 필터 공고 번호");
            query.addCriteria(bidNumCriteria);
        }
    }

    // 공고 이름 조건을 추가하는 메서드
    private void applyBidNameFilter(BidSearchRequestDto requestDto, Query query) {
        if (requestDto.bidName() != null && !requestDto.bidName().isEmpty()) {  // null과 "" 이 아니라면
            Criteria bidNameCriteria = new Criteria().orOperator(
                    Criteria.where("bidNtceNm").regex(requestDto.bidName(), "i")    // 공고명 검색 (대소문자 무시)
            );
            log.debug("검색 필터 공고명");
            query.addCriteria(bidNameCriteria);
        }
    }

    // 업무 구분 조건을 추가하는 메서드
    private void applyTaskTypeFilter(BidSearchRequestDto requestDto, Query query) {
        if (requestDto.taskType() != null && !requestDto.taskType().isEmpty()) {
            log.debug("검색 필터 업무 구분");
            query.addCriteria(Criteria.where("bsnsDivNm").is(requestDto.taskType()));   // 같은 String 값 조회
        }
    }

    // 업종 조건을 추가하는 메서드
    private void applyIndustryFilter(BidSearchRequestDto requestDto, Query query) {
        if (requestDto.indstryty() != null) {
            // 1️⃣ MySQL에서 업종코드 리스트 조회
            List<String> industryCodes = industryRepository.findIndustryCodesByMajorCategory(requestDto.indstryty());
            log.debug(industryCodes.toString());

            if (!industryCodes.isEmpty()) {
                log.debug("업종코드가 포함된 공고 데이터 조회");
                // 2️⃣ MongoDB에서 업종코드가 포함된 공고 데이터 조회
                query.addCriteria(Criteria.where("indstrytyCd").in(industryCodes));
//                query.addCriteria(Criteria.where("indstrytyNm").in(industryCodes));
            }
        }
    }

    // 기관선택 조건을 추가하는 메서드
    private void applyAgencyChoiceFilter(BidSearchRequestDto requestDto, Query query) {
        if (requestDto.ntceInsttNm() != null && !requestDto.ntceInsttNm().isEmpty()){   // 검색어가 있다면
            log.debug("검색 필터 기관");
            query.addCriteria(Criteria.where("ntceInsttNm").regex(requestDto.ntceInsttNm(), "i")); // 대소문자 무시
        } else if (requestDto.dminsttNm() != null && !requestDto.dminsttNm().isEmpty()) {
            log.debug("검색 필터 기관");
            query.addCriteria(Criteria.where("dminsttNm").regex(requestDto.dminsttNm(), "i"));
        }
    }

    // 날짜 범위 조건을 추가하는 메서드 (진행 중)
    private void applyDateFilter(BidSearchRequestDto requestDto, Query query) {
        if(requestDto.getMinDateTime() != null)     log.debug(requestDto.getMinDateTime().toString());
        if (requestDto.getMaxDateTime() != null)    log.debug(requestDto.getMaxDateTime().toString());

        if (requestDto.getMinDateTime() != null && requestDto.getMaxDateTime() != null) {
            log.debug("검색 필터 날짜");
            query.addCriteria(Criteria.where("bidNtceDt").gte(requestDto.getMinDateTime()).lte(requestDto.getMaxDateTime()));
        } else if (requestDto.getMinDateTime() != null) {
            log.debug("검색 필터 날짜");
            query.addCriteria(Criteria.where("bidNtceDt").gte(requestDto.getMinDateTime()));
        } else if (requestDto.getMaxDateTime() != null) {
            log.debug("검색 필터 날짜");
            query.addCriteria(Criteria.where("bidNtceDt").lte(requestDto.getMaxDateTime()));
        }
    }

    // 예산 범위 조건을 추가하는 메서드
    private void applyBudgetFilter(BidSearchRequestDto requestDto, Query query) {
        if (requestDto.minPresmptPrce() != null && requestDto.maxPresmptPrce() != null) {
            log.debug("검색 필터 예산 범위");
            query.addCriteria(Criteria.where("presmptPrce").gte(requestDto.minPresmptPrce()).lte(requestDto.maxPresmptPrce()));
        } else if (requestDto.minPresmptPrce() != null) {
            log.debug("검색 필터 예산 범위");
            query.addCriteria(Criteria.where("presmptPrce").gte(requestDto.minPresmptPrce()));
        } else if (requestDto.maxPresmptPrce() != null) {
            log.debug("검색 필터 예산 범위");
            query.addCriteria(Criteria.where("presmptPrce").lte(requestDto.maxPresmptPrce()));
        }
    }

    // 지역 조건을 추가하는 메서드
    private void applyRegionFilter(BidSearchRequestDto requestDto, Query query) {
        if (requestDto.region() != null && !requestDto.region().isEmpty()) {
            log.debug("검색 필터 지역 : " + requestDto.region());
            String regexPattern = ".*" + requestDto.region() + ".*";
            query.addCriteria(Criteria.where("prtcptPsblRgnNm").regex(regexPattern, "i")); // 대소문자 무시
        }
    }


    // 정렬 조건을 처리하는 메서드
    private void applySorting(BidSearchRequestDto requestDto, Query query) {
        // 기본 정렬 최신순
        String sortOption = (requestDto.sort() != null) ? requestDto.sort() : "최신순"; // null 이면 최신순
        Sort sort = Sort.by(Sort.Order.desc("bidNtceDt"));      // 최신순 기본값
        switch (sortOption) {
            case "과거순":
                log.debug("과거순");
                sort = Sort.by(Sort.Order.asc("bidNtceDt"));
                break;
            case "금액높은순":
                log.debug("금액높은순");
                sort = Sort.by(Sort.Order.desc("presmptPrce"));
                break;
            case "금액낮은순":
                log.debug("금액낮은순");
                sort = Sort.by(Sort.Order.asc("presmptPrce"));
                break;
        }
        query.with(sort);
    }


    private List<Document> findDocumentsBySearchOption(Query query) {
        query.fields()
                .include("_id") // bidNtceId
                .include("bidNtceNo")
                .include("bidNtceOrd")
                .include("bidNtceNm")
                .include("bsnsDivNm")
                .include("cntrctCnclsMthdNm")
                .include("bidNtceDt")
                .include("bidClseDt")
                .include("dminsttNm")
                .include("presmptPrce")
                ;

        return mongoTemplate.find(query, Document.class, "bids");
    }
}
