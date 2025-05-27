package com.naratil.batch.tasklet;

import com.naratil.batch.client.fastapi.UpdateIndexApiClient;
import com.naratil.batch.common.MongoCollectionChecker;
import com.naratil.batch.dto.fastapi.indexing.AddIndexRequest;
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
 * 과거 공고 컬렉션 벡터 FAISS 인덱싱 Tasklet
 * 컬렉션별로 1000개씩 끊어서 FastAPI에 인덱스 추가 요청
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreatePastIndexTasklet implements Tasklet {

    private final MongoTemplate mongoTemplate;
    private final MongoCollectionChecker collectionChecker;
    private final UpdateIndexApiClient addIndexApiClient;

//    private static final int BATCH_SIZE = 1000;
    private static final int BATCH_SIZE = 50;

    @Override
    public RepeatStatus execute(@Nonnull StepContribution contribution, @Nonnull ChunkContext chunkContext) {
        log.debug("[Tasklet] 과거 공고 인덱싱 시작");

        List<String> collectionNames = new ArrayList<>(collectionChecker.getCachedCollections());

        int collectionCount = 0;
        for (String collectionName : collectionNames) {
            if (!collectionName.startsWith("industry_")) {
                continue; // 과거 공고 컬렉션만
            }

            log.debug("[Tasklet] 과거 공고 컬렉션 처리 시작: {}", collectionName);

            try (var cursor = mongoTemplate.getCollection(collectionName)
                    .find(buildQuery())
                    .projection(buildProjection())
                    .batchSize(BATCH_SIZE)
                    .iterator()) {

                Map<Long, List<String>> keywordsMap = new HashMap<>();
                List<List<Float>> vectors = new ArrayList<>();
                List<Long> ids = new ArrayList<>();

                int total = 0;

                while (cursor.hasNext()) {
                    Document doc = cursor.next();
                    Long id = (long)doc.getInteger("bidNtceId");
                    keywordsMap.put(id, (List<String>)doc.get("keywords"));
                    vectors.add(VectorUtils.toFloatList(doc.get("vectorNm")));
                    ids.add(id);
                    total++;

                    if (vectors.size() >= BATCH_SIZE) {
                        sendBatch(collectionName, keywordsMap, vectors, ids);
                        keywordsMap.clear();
                        vectors.clear();
                        ids.clear();
                    }
                }

                // 남은 것 처리
                if (!vectors.isEmpty()) {
                    sendBatch(collectionName, keywordsMap, vectors, ids);
                }

                collectionCount++;
                log.info("[Tasklet] 과거 공고 컬렉션 {} - 전체 {}건 인덱싱 완료", collectionName, total);

            } catch (Exception e) {
                log.error("[Tasklet] 과거 공고 컬렉션 {} 인덱싱 실패", collectionName, e);
                throw new RuntimeException("과거 공고 인덱싱 실패 - collection: " + collectionName, e);
            }
        }

        log.info("[Tasklet] 과거 공고 인덱싱 전체 완료 - 컬렉션 {}개", collectionCount);
        return RepeatStatus.FINISHED;
    }

    private void sendBatch(String collectionName, Map<Long, List<String>> keywordsMap, List<List<Float>> vectors, List<Long> ids) {
        AddIndexRequest request = new AddIndexRequest();
        request.setCollectionName(collectionName);
        request.setKeywordsMap(keywordsMap);
        request.setVectors(vectors);
        request.setIds(ids);

        addIndexApiClient.addIndex(request, "past");
        log.debug("[Tasklet] 과거 공고 {} - 인덱스 추가 완료 ({}건)", collectionName, ids.size());
    }

    private Document buildQuery() {
        String startDate = LocalDateTime.now().minusYears(1).toString();
        return new Document("bidClseDt", new Document("$gte", startDate))
                .append("keywords", new Document("$exists", true))
                .append("vectorNm", new Document("$exists", true))
                .append("bidNtceId", new Document("$exists", true))
                ;
    }

    private Document buildProjection() {
        return new Document()
                .append("bidNtceId", 1)
                .append("vectorNm", 1)
                .append("keywords", 1)
                ;
    }
}
