package com.naratil.batch.processor;

import com.naratil.batch.client.fastapi.RecommendApiClient;
import com.naratil.batch.common.MongoCollectionChecker;
import com.naratil.batch.dto.fastapi.recommend.RecommendNtcesRequest;
import com.naratil.batch.dto.fastapi.recommend.RecommendNtcesResponse;
import com.naratil.batch.dto.fastapi.recommend.RecommendedNtceScore;
import com.naratil.batch.dto.fastapi.common.NtceForComparison;
import com.naratil.batch.dto.fastapi.common.SimilarNtceCondition;
import com.naratil.batch.model.Corporation;
import com.naratil.batch.model.Recommend;
import com.naratil.batch.util.VectorUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RecommendProcessor implements ItemProcessor<List<Corporation>, List<Recommend>> {

    private final MongoTemplate mongoTemplate;
    private final RecommendApiClient apiClient;
    private final MongoCollectionChecker collectionChecker;
    private final SimilarNtceCondition condition;

    public RecommendProcessor(MongoTemplate mongoTemplate,
                              RecommendApiClient apiClient,
                              MongoCollectionChecker collectionChecker) {
        this.mongoTemplate = mongoTemplate;
        this.apiClient = apiClient;
        this.collectionChecker = collectionChecker;
        this.condition = new SimilarNtceCondition(
                100,  // 코사인 유사도 상위 k
                100,  // 코사인 + 자카드 유사도 상위 k
                0.2f, // 자카드 가중치
                0.8f, // 코사인 가중치
                0.7f, // 코사인 하한값
                0.7f, // 최소 점수
                4     // 최종 반환 수
        );
    }

    @Override
    public List<Recommend> process(List<Corporation> corporations) {
        if (corporations.isEmpty()) {
            log.warn("[Processor] 처리할 기업이 없습니다.");
            return Collections.emptyList();
        }

        log.debug("[Processor] 총 {}개 기업 추천 처리 시작", corporations.size());

//        // 중복 기업 제거
//        List<Corporation> distinctCorporations = corporations.stream()
//                .collect(Collectors.toMap(
//                        Corporation::getCorpId,
//                        corp -> corp,
//                        (corp1, corp2) -> corp1
//                ))
//                .values()
//                .stream()
//                .toList();
//
//        log.debug("[Processor] 중복 제거 후 기업 수: {}", distinctCorporations.size());

        List<Recommend> totalResults = new ArrayList<>();

        int bidCorpCount = 0; // 기업수 - 입찰 정보가 없는 기업 제외
        int recommendCorpCount = 0; // 기업수 - 실제 추천 결과가 있는 기업(추천 결과 0건 제외)

        // 기업 하나씩 FastAPI 호출 - 부하 줄이기 위해서 순차 호출로 변경
        for (Corporation corporation : corporations) {
            Map<Long, List<NtceForComparison>> corpPastNtces = fetchPastNtcesByBizno(List.of(corporation));

            if (corpPastNtces.isEmpty()) {
                log.warn("[Processor] 과거 입찰 정보 없음(기업ID: {}, 사업자등록번호: {})", corporation.getCorpId(), corporation.getBizno());
                continue;
            }

            RecommendNtcesRequest request = new RecommendNtcesRequest();
            request.setCorpNtces(corpPastNtces);
            request.setSimilarNtceCondition(condition);

            try {
                RecommendNtcesResponse response = apiClient.getRecommendNtces(request);

                if (response != null && response.getRecommendedNtces() != null) {
                    List<RecommendedNtceScore> scores = response.getRecommendedNtces().get(corporation.getCorpId());
                    if (scores != null) {
                        Set<Long> alreadyRecommended = new HashSet<>();

                        if(scores.isEmpty()){
                            continue;
                        }

                        for (RecommendedNtceScore score : scores) {
                            if (!alreadyRecommended.add(score.getBidNtceId())) {
                                continue; // 같은 공고는 무시
                            }
                            totalResults.add(new Recommend(corporation.getCorpId(), score.getBidNtceId(), score.getAdjustScore()));
                        }

                        recommendCorpCount++;
                    }
                }
                bidCorpCount++;

            } catch (Exception e) {
                log.error("[Processor] FastAPI 추천 호출 실패 (기업ID: {}, 사업자등록번호: {})", corporation.getCorpId(), corporation.getBizno(), e);
            }
        }

        log.info("[Processor] 추천 결과 총 {}건 - 총 추천 완료 기업 {}/{}개(입찰 정보 보유 {}개)", totalResults.size(), recommendCorpCount, corporations.size(), bidCorpCount);
        return totalResults;
    }

    /**
     * MongoDB에서 과거 낙찰 기록 조회
     */
    private Map<Long, List<NtceForComparison>> fetchPastNtcesByBizno(List<Corporation> corporations) {
        Map<Long, List<NtceForComparison>> map = new HashMap<>();

        for (Corporation corporation : corporations) {
            List<NtceForComparison> result = new ArrayList<>();

            try {
                Document companyDoc = mongoTemplate.getCollection("company_full")
                        .find(new Document("prcbdrBizno", corporation.getBizno()))
                        .projection(new Document("bidsList", 1))
                        .first();

                if (companyDoc != null && companyDoc.get("bidsList") != null) {
                    List<List<String>> bidsList = (List<List<String>>) companyDoc.get("bidsList");

                    Map<String, List<String>> collectionToBidNos = new HashMap<>();
                    for (List<String> bid : bidsList) {
                        if (bid.size() >= 2) {
                            String bidNtceNo = bid.get(0);
                            String industryCd = bid.get(1);
                            String collectionName = collectionChecker.resolveIndustryCollection(industryCd);

                            collectionToBidNos.computeIfAbsent(collectionName, k -> new ArrayList<>()).add(bidNtceNo);
                        }
                    }

                    for (Map.Entry<String, List<String>> entry : collectionToBidNos.entrySet()) {
                        List<Document> docs = mongoTemplate.getCollection(entry.getKey())
                                .find(new Document("bidNtceNo", new Document("$in", entry.getValue())))
                                .projection(new Document("bidNtceId", 1)
                                        .append("bidNtceNm", 1)
                                        .append("vectorNm", 1)
                                        .append("keywords", 1))
                                .into(new ArrayList<>());

                        for (Document doc : docs) {
                            result.add(new NtceForComparison(
                                    (long) doc.getInteger("bidNtceId"),
                                    doc.getString("bidNtceNm"),
                                    VectorUtils.toFloatList(doc.get("vectorNm")),
                                    (List<String>) doc.get("keywords"),
                                    entry.getKey()
                            ));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[Processor] 과거 입찰 공고 조회 실패 - 사업자등록번호: {}", corporation.getBizno(), e);
            }

            if (!result.isEmpty()) {
                log.debug("[Processor] 과거 입찰 정보 {}건, (기업ID: {}, 사업자등록번호: {})", result.size(), corporation.getCorpId(), corporation.getBizno());
                map.put(corporation.getCorpId(), result);
            }
        }

        return map;
    }
}
