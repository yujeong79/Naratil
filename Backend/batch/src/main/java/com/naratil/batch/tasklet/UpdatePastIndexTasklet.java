package com.naratil.batch.tasklet;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.naratil.batch.client.fastapi.UpdateIndexApiClient;
import com.naratil.batch.common.MongoCollectionChecker;
import com.naratil.batch.dto.fastapi.indexing.AddIndexRequest;
import com.naratil.batch.util.KeywordExtractor;
import com.naratil.batch.util.VectorUtils;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 과거 공고 컬렉션 벡터 FAISS 인덱싱 추가 Tasklet
 * 1000개씩 끊어서 FastAPI에 인덱스 추가 요청
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdatePastIndexTasklet implements Tasklet {

    private final MongoTemplate mongoTemplate;
    private final MongoCollectionChecker collectionChecker;
    private final KeywordExtractor keywordExtractor;
    private final UpdateIndexApiClient addIndexApiClient;

    private static final int BATCH_SIZE = 100;

    @Override
    public RepeatStatus execute(@Nonnull StepContribution contribution, @Nonnull ChunkContext chunkContext) {
        log.debug("[Tasklet] 일일 과거 공고 인덱스 추가 시작");
        int collectionCount = 0;

        List<String> collectionNames = new ArrayList<>(collectionChecker.getCachedCollections());

        for (String collectionName : collectionNames) {
            if (!collectionName.startsWith("industry_")) {
                continue; // 과거 공고 컬렉션만
            }

            log.debug("[Tasklet] 일일 과거 공고 인덱스 추가 컬렉션 처리 시작: {}", collectionName);

            try (var cursor = mongoTemplate.getCollection(collectionName)
                    .find(buildQuery())
                    .projection(buildProjection())
                    .batchSize(BATCH_SIZE)
                    .iterator()) {

                List<WriteModel<Document>> updates = new ArrayList<>();

                Map<Long, List<String>> keywordsMap = new HashMap<>();
                List<List<Float>> vectors = new ArrayList<>();
                List<Long> ids = new ArrayList<>();

                int total = 0;

                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    Long id = (long)doc.getInteger("bidNtceId");
                    List<String> keywords = keywordExtractor.extractCleanKeywords(doc.getString("bidNtceNm"));

                    keywordsMap.put(id, keywords);
                    vectors.add(VectorUtils.toFloatList(doc.get("vectorNm")));
                    ids.add(id);

                    Document filter = new Document("bidNtceId", id);
                    Document updateFields = new Document("keywords", keywords);
                    Document update = new Document("$set", updateFields);

                    updates.add(new UpdateOneModel<>(filter, update));

                    total++;

                    if (vectors.size() >= BATCH_SIZE) {
                        sendBatch(collectionName, keywordsMap, vectors, ids);
                        bulkupdate(collectionName, updates);
                        updates.clear();
                        keywordsMap.clear();
                        vectors.clear();
                        ids.clear();
                    }
                }

                // 남은 것 처리
                if (!vectors.isEmpty()) {
                    sendBatch(collectionName, keywordsMap, vectors, ids);
                    bulkupdate(collectionName, updates);
                }

                log.info("[Tasklet] 과거 공고 인덱스 추가 컬렉션 {} - 전체 {}건 인덱싱 완료", collectionName, total);
                collectionCount++;

            } catch (Exception e) {
                log.error("[Tasklet] 과거 공고 인덱스 추가 컬렉션 {} 추가 실패", collectionName, e);
                throw new RuntimeException("인덱스 추가 실패 - collection: " + collectionName, e);
            }
        }

        log.debug("[Tasklet] 과거 공고 인덱스 추가 과거 공고 인덱싱 전체 완료 - 컬렉션 {}개", collectionCount);
        return RepeatStatus.FINISHED;
    }

    private void sendBatch(String collectionName, Map<Long, List<String>> keywordsMap, List<List<Float>> vectors, List<Long> ids) {
        AddIndexRequest request = new AddIndexRequest();
        request.setCollectionName(collectionName);
        request.setKeywordsMap(keywordsMap);
        request.setVectors(vectors);
        request.setIds(ids);

        addIndexApiClient.addIndex(request, "past");
        log.debug("[Tasklet] 과거 공고 인덱스 추가 {} - 추가 완료 ({}건)", collectionName, ids.size());
    }

    private Document buildQuery() {
        String startDate = LocalDateTime.now().minusYears(1).toString();
        return new Document("bidClseDt", new Document("$gte", startDate))
                .append("keywords", new Document("$exists", false))
                .append("vectorNm", new Document("$exists", true))
                .append("bidNtceId", new Document("$exists", true))
                ;
    }

    private Document buildProjection() {
        return new Document()
                .append("bidNtceId", 1)
                .append("vectorNm", 1)
                .append("bidNtceNm", 1) // 키워드 추출용
                ;
    }

    private void bulkupdate(String collectionName, List<WriteModel<Document>> updates){
        BulkWriteResult result = mongoTemplate.getDb()
                .getCollection(collectionName) // 컬렉션 - BidNotice
                .bulkWrite(updates, new BulkWriteOptions().ordered(false)); // 순서 상관없이

        log.debug("[Tasklet] 일일 과거 공고 인덱스 추가 Bulk Update 완료 → matched: {}, modified: {}", result.getMatchedCount(), result.getModifiedCount());
    }
}
