package com.naratil.bid.service;

import com.naratil.bid.dto.BidDetailResponseDto;
import java.util.ArrayList;
import java.util.List;

import com.naratil.common.cache.MongoCollectionChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidNoticeDetailService {

    private final MongoTemplate mongoTemplate;
    private final MongoCollectionChecker collectionChecker;

    public BidDetailResponseDto getNoticeById(long bidId) {
        // Step 1. 현재 공고 조회
        Document currentBids = findDocumentByCurrentId(bidId);
        log.debug("getNoticeById, 현재공고 currentBids: {}", currentBids);

        if (currentBids == null) {
            throw new RuntimeException("해당 ID의 공고가 없습니다.");
        }

        // Step 3. 유사 공고 리스트 추출
        List<Document> similarNtces = (List<Document>) currentBids.get("similarNtces");
//        List<SimilarNtceItem> similarNtces = (List<SimilarNtceItem>) currentBids.get("similarNtces");
//        log.debug("업종코드 : {}", currentBids.getString("indstrytyCd"));
//        log.debug("getNoticeById, similarNtces: {}", similarNtces);

        // Step 4. 유사 공고를 업종 코드별 컬렉션에서 조회
        List<Document> pastData = new ArrayList<>();
        if (similarNtces != null && !similarNtces.isEmpty()) {
            for (Document similar : similarNtces) {
                int pastId = similar.getLong("bidNtceId").intValue();
                String collectionName = null;

                if(Boolean.TRUE.equals(similar.getBoolean("collectionNoLimit"))){
                    collectionName = "industry_no_limit";
                }else{
                    collectionName = collectionChecker.resolveIndustryCollection(currentBids.getString("indstrytyCd"));
                }
                Document past = findDocumentByPastId(pastId, collectionName);
                if (past != null) {
                    pastData.add(past);
                }
            }
        }

        currentBids.remove("similarNtces"); // 삭제 - 따로
        log.debug("리턴 곧 시작");
        return BidDetailResponseDto.builder()
                .currentBids(currentBids)
                .pastData(pastData)
                .build();
    }

    private Document findDocumentByCurrentId(long id) {
        Query query = Query.query(Criteria.where("_id").is(id));

        query.fields()
                .exclude("isUpdated")
                .exclude("createdAt")
                .exclude("updatedAt")
                .exclude("vector")
                .exclude("keywords")

                .exclude("similarNtces.bidNtceNm")
                .exclude("similarNtces.finalScore")
                .exclude("similarNtces.cosineScore")
                .exclude("similarNtces.jaccardScore")
        ;

        return mongoTemplate.findOne(query, Document.class, "bids");
    }

    public Document findDocumentByPastId(int id, String collectionName) {
        Query query = Query.query(Criteria.where("bidNtceId").is(id));

        query.fields()
                .exclude("_id")
                .exclude("vectorNm")
                .exclude("keywords")
                ;
        return mongoTemplate.findOne(query, Document.class, collectionName);
    }
}
