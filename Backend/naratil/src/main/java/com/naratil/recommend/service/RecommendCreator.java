package com.naratil.recommend.service;

import com.naratil.common.cache.MongoCollectionChecker;
import com.naratil.common.client.CorpRecommendApiClient;
import com.naratil.common.util.VectorUtils;
import com.naratil.corporation.entity.Corporation;
import com.naratil.recommend.dto.*;
import com.naratil.recommend.entity.Recommend;
import com.naratil.recommend.repository.RecommendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendCreator {

    private final MongoTemplate mongoTemplate;
    private final MongoCollectionChecker collectionChecker;
    private final CorpRecommendApiClient apiClient;
    private final RecommendRepository repository;

    /**
     * 새로 등록된 기업 추천
     * @param corp
     */
    public void createRecommend(Corporation corp) {
        log.info("새로 등록된 기업 추천 실행 (기업ID: {}, 기업명: {})", corp.getCorpid(), corp.getCorpNm());

        List<NtceForComparison> corpPastNtces = fetchPastNtcesByBizno(corp.getBizno());

        if (corpPastNtces.isEmpty()) {
            log.info("기업의 과거 입찰 기록이 없습니다. (사업자등록번호: {})", corp.getBizno());
            return;
        }

        List<Recommend> recommends = generateRecommends(corpPastNtces, corp);
        saveRecommends(recommends);
    }

    private List<NtceForComparison> fetchPastNtcesByBizno(String bizno) {
        try {
            Document companyDoc = findCompanyDocument(bizno);

            if (companyDoc == null || companyDoc.get("bidsList") == null) {
                return Collections.emptyList();
            }

            Map<String, List<String>> collectionToBidNos = groupBidsByCollection((List<List<String>>) companyDoc.get("bidsList"));

            return fetchBidNotices(collectionToBidNos);
        } catch (Exception e) {
            log.error("기업의 과거 입찰 공고 조회 실패 (사업자등록번호: {})", bizno, e);
            return Collections.emptyList();
        }
    }

    /**
     * 기업의 과거 입찰 기록
     * @param bizno
     * @return
     */
    private Document findCompanyDocument(String bizno) {
        return mongoTemplate.getCollection("company_full")
                .find(new Document("prcbdrBizno", bizno))
                .projection(new Document("bidsList", 1))
                .first();
    }

    /**
     * 과거 입찰 기록 -> 컬렉션으로 묶기
     * @param bidsList
     * @return
     */
    private Map<String, List<String>> groupBidsByCollection(List<List<String>> bidsList) {
        Map<String, List<String>> collectionToBidNos = new HashMap<>();

        for (List<String> bid : bidsList) {
            if (bid.size() >= 2) {
                String bidNtceNo = bid.get(0);
                String industryCd = bid.get(1);
                String collectionName = collectionChecker.resolveIndustryCollection(industryCd);

                collectionToBidNos.computeIfAbsent(collectionName, k -> new ArrayList<>()).add(bidNtceNo);
            }
        }

        return collectionToBidNos;
    }

    /**
     * 컬렉션별 과거 입찰 기록 가져오기
     * @param collectionToBidNos
     * @return
     */
    private List<NtceForComparison> fetchBidNotices(Map<String, List<String>> collectionToBidNos) {
        List<NtceForComparison> result = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : collectionToBidNos.entrySet()) {
            Query query = new Query(Criteria.where("bidNtceNo").in(entry.getValue()));
            query.fields().include("bidNtceId").include("bidNtceNm").include("vectorNm").include("keywords");

            List<Document> docs = mongoTemplate.find(query, Document.class, entry.getKey());

            for (Document doc : docs) {
                result.add(toNtceForComparison(doc, entry.getKey()));
            }
        }

        return result;
    }


    /**
     * 추천을 위한 DTO로 변환
     * @param doc
     * @param collectionName
     * @return
     */
    private NtceForComparison toNtceForComparison(Document doc, String collectionName) {
        return new NtceForComparison(
                (long) doc.getInteger("bidNtceId"),
                doc.getString("bidNtceNm"),
                VectorUtils.toFloatList(doc.get("vectorNm")),
                (List<String>) doc.get("keywords"),
                collectionName
        );
    }

    /**
     * 추천 생성
     * @param corpPastNtces
     * @param corp
     * @return
     */
    private List<Recommend> generateRecommends(List<NtceForComparison> corpPastNtces, Corporation corp) {
        Map<Long, List<NtceForComparison>> corpPastNtcesWithId = Map.of(corp.getCorpid(), corpPastNtces);
        SimilarNtceCondition condition = createSimilarNtceCondition();

        RecommendNtcesRequest request = new RecommendNtcesRequest();
        request.setCorpNtces(corpPastNtcesWithId);
        request.setSimilarNtceCondition(condition);

        try {
            RecommendNtcesResponse response = apiClient.getRecommendNtces(request);
            return extractRecommends(response, corp);
        } catch (Exception e) {
            log.error("기업 추천 호출 실패 (기업ID: {}, 기업명: {})", corp.getCorpid(), corp.getCorpNm(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 추천 산출 조건
     * @return
     */
    private SimilarNtceCondition createSimilarNtceCondition() {
        return new SimilarNtceCondition(100, 100, 0.2f, 0.8f, 0.7f, 0.7f, 4);
    }

    /**
     * 결과에서 Entity 객체 리스트로 변환
     * @param response
     * @param corp
     * @return
     */
    private List<Recommend> extractRecommends(RecommendNtcesResponse response, Corporation corp) {
        if (response == null || response.getRecommendedNtces() == null) {
            return Collections.emptyList();
        }

        List<RecommendedNtceScore> scores = response.getRecommendedNtces().get(corp.getCorpid());
        if (scores == null || scores.isEmpty()) {
            return Collections.emptyList();
        }

        List<Recommend> recommends = new ArrayList<>();
        for (RecommendedNtceScore score : scores) {
            recommends.add(Recommend.builder()
                    .corporation(corp)
                    .bidNtceId(score.getBidNtceId())
                    .score(score.getAdjustScore())
                    .build());
        }

        return recommends;
    }

    /**
     * RDB에 추천 정보 Bulk Insert
     * @param recommends
     */
    private void saveRecommends(List<Recommend> recommends) {
        if (!recommends.isEmpty()) {
            repository.saveAll(recommends);
            log.info("추천 결과 {}건 저장 완료", recommends.size());
        }
    }
}
