package com.naratil.batch.writer;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.naratil.batch.client.fastapi.SimilarityApiClient;
import com.naratil.batch.common.MongoCollectionChecker;
import com.naratil.batch.dto.fastapi.common.NtceForComparison;
import com.naratil.batch.dto.fastapi.common.SimilarNtceCondition;
import com.naratil.batch.dto.fastapi.similarity.*;
import com.naratil.batch.model.BidNotice;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 유사 공고 업데이트 Writer
 * - BidNotice -> NtceComparison 변환
 * - FastAPI Similarity 호출
 * - MongoDB Bulk Update
 */
@Slf4j
@Component
public class SimilarNtcesWriter implements ItemWriter<List<BidNotice>> {

    private static final int BATCH_SIZE = 50; // FastAPI 호출 단위

    private final MongoTemplate mongoTemplate;
    private final SimilarityApiClient apiClient;
    private final MongoCollectionChecker collectionChecker;
    private final SimilarNtceCondition condition;

    public SimilarNtcesWriter(MongoTemplate mongoTemplate, SimilarityApiClient apiClient, MongoCollectionChecker collectionChecker) {
        this.mongoTemplate = mongoTemplate;
        this.apiClient = apiClient;
        this.collectionChecker = collectionChecker;
        this.condition = new SimilarNtceCondition(
                100, // 코사인 유사도 상위 k
                3,   // 코사인 + 자카드 유사도 상위 k
                0.2f, // 자카드 가중치
                0.8f, // 코사인 가중치
                0.66f,    // 코사인 하한값
                0.75f, // 최소 점수(정규 기준)
                3    // 최종 산출 개수
        );
    }

    @Override
    public void write(Chunk<? extends List<BidNotice>> chunk) {
        List<BidNotice> notices = chunk.getItems().stream()
                .flatMap(List::stream)
                .toList();

        if (notices.isEmpty()) {
            log.warn("[Writer] 유사 공고 입력 데이터 없음 (스킵)");
            return;
        }

        // 200개씩 나눔
        List<List<BidNotice>> batches = partition(notices, BATCH_SIZE);
        log.debug("[Writer] 유사 공고 전체 {}개 공고 → {}개 배치로 분할 완료", notices.size(), batches.size());


        for (int i = 0; i < batches.size(); i++) {
            List<BidNotice> batch = batches.get(i);

            log.debug("[Writer] 유사 공고 배치 {}/{} 시작 - 공고 수: {}", i + 1, batches.size(), batch.size());

            // BidNotice → NtceComparison 변환
            List<NtceForComparison> comparisons = batch.stream()
                    .map(this::toComparison)
                    .toList();

            // FastAPI 호출
            SimilarNtcesRequest request = new SimilarNtcesRequest(comparisons, condition);

            SimilarNtcesResponse response;
            try {
                response = apiClient.getSimilarNtces(request);
            } catch (Exception e) {
                log.error("[Writer] 유사 공고 FastAPI 호출 실패 - 배치 {}/{}", i + 1, batches.size(), e);
                continue;
            }

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                log.warn("[Writer] 유사 공고 FastAPI 응답 없음 - 배치 {}/{}", i + 1, batches.size());
                continue;
            }

            // MongoDB Bulk Update
            bulkUpdateSimilarNotices(response.getResults());
            log.debug("[Writer] 유사 공고 배치 {}/{} 완료", i + 1, batches.size());

        }

        log.info("[Writer] 유사 공고 - 현재 공고 {}건 저장 완료", notices.size());
    }

    /**
     * BidNotice -> NtceComparison 변환
     */
    private NtceForComparison toComparison(BidNotice notice) {
        String collectionName = collectionChecker.resolveIndustryCollection(notice.getIndstrytyCd());
        return new NtceForComparison(
                notice.getBidNtceId(),
                notice.getBidNtceNm(),
                notice.getVector(),
                notice.getKeywords(),
                collectionName
        );
    }

    /**
     * 리스트 분할
     */
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    /**
     * MongoDB Bulk Update
     */
    private void bulkUpdateSimilarNotices(Map<Long, List<SimilarNtceItem>> similarResults) {
        if (similarResults.isEmpty()) {
            log.warn("[Writer] 업데이트할 응답이 없습니다. (스킵)");
            return;
        }

        MongoCollection<Document> collection = mongoTemplate.getCollection("bids");
        List<WriteModel<Document>> bulkUpdates = new ArrayList<>();

        Set<Long> allIds = new HashSet<>();
        Map<Long, String> idToCollection = new HashMap<>();

        for (List<SimilarNtceItem> items : similarResults.values()) {
            for (SimilarNtceItem item : items) {
                allIds.add(item.getBidNtceId());
                idToCollection.put(item.getBidNtceId(), item.getCollectionName());
            }
        }

        Map<Long, String> idToName = new HashMap<>();
        Map<String, List<Long>> idsByCollection = new HashMap<>();

        for (Map.Entry<Long, String> entry : idToCollection.entrySet()) {
            idsByCollection.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        for (Map.Entry<String, List<Long>> entry : idsByCollection.entrySet()) {
            String collectionName = entry.getKey();
            List<Long> ids = entry.getValue();

            if (collectionName == null || ids.isEmpty()) {
                continue;
            }

            List<Document> docs = mongoTemplate.getCollection(collectionName)
                    .find(new Document("bidNtceId", new Document("$in", ids)))
                    .projection(new Document("bidNtceId", 1).append("bidNtceNm", 1))
                    .into(new ArrayList<>());

            for (Document doc : docs) {
                idToName.put((long)doc.getInteger("bidNtceId"), doc.getString("bidNtceNm"));
            }
        }

        for (Map.Entry<Long, List<SimilarNtceItem>> entry : similarResults.entrySet()) {
            Long bidNtceId = entry.getKey();
            List<SimilarNtceItem> items = entry.getValue();

            List<Document> itemDocs = items.stream()
                    .map(item -> toDocument(item, idToName))
                    .toList();

            Document filter = new Document("_id", bidNtceId);
            Document update = new Document("$set", new Document("similarNtces", itemDocs));

            bulkUpdates.add(new UpdateOneModel<>(filter, update));
        }

        if (!bulkUpdates.isEmpty()) {
            try {
                var result = collection.bulkWrite(bulkUpdates);
                log.debug("[Writer] 유사 공고 MongoDB Bulk 업데이트 완료 - 수정 건수: {}", result.getModifiedCount());
            } catch (Exception e) {
                log.error("[Writer] 유사 공고 MongoDB Bulk 업데이트 실패", e);
            }
        } else {
            log.warn("[Writer] Bulk 업데이트할 유사 공고가 없습니다.");
        }
    }


    /**
     * SimilarNtceItem → MongoDB Document 변환
     */
    private Document toDocument(SimilarNtceItem item, Map<Long, String> idToName) {
        Document doc = new Document();
        doc.append("bidNtceId", item.getBidNtceId());

        String name = idToName.get(item.getBidNtceId());
        if (name != null) {
            doc.append("bidNtceNm", name);
        } else {
            log.warn("[Writer] 이름 매칭 실패 - bidNtceId: {}", item.getBidNtceId());
        }

        doc.append("finalScore", item.getFinalScore());
        doc.append("cosineScore", item.getCosineScore());
        doc.append("jaccardScore", item.getJaccardScore());
        return doc;
    }


    /**
     * ID로 컬렉션 타고 이름 찾기
     */
    private String findBidNtceNm(String collectionName, Long bidNtceId) {
        if (collectionName == null || bidNtceId == null) {
            log.warn("[Writer] 유사 공고 공고명 조회 실패 - collectionName 또는 bidNtceId가 null입니다.");
            return null;
        }

        try {
            Document doc = mongoTemplate.getCollection(collectionName)
                    .find(new Document("bidNtceId", bidNtceId))
                    .projection(new Document("bidNtceNm", 1))
                    .first();

            if (doc != null) {
                return doc.getString("bidNtceNm");
            } else {
                log.warn("[Writer] 컬렉션 {} 에서 공고 ID {} 에 해당하는 이름을 찾지 못했습니다.", collectionName, bidNtceId);
                return null;
            }
        } catch (Exception e) {
            log.error("[Writer] 공고명 조회 실패 - collectionName: {}, bidNtceId: {}", collectionName, bidNtceId, e);
            return null;
        }
    }
}
