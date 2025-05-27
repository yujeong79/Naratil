package com.naratil.batch.reader;

import com.mongodb.client.MongoCursor;
import com.naratil.batch.dto.fastapi.common.NtceBase;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.item.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 현재 공고 Reader
 * - vector 또는 keywords 누락된 공고만 조회
 */
@Slf4j
@RequiredArgsConstructor
public class NtceEmbeddingReader implements ItemReader<List<NtceBase>>, ItemStream {

    private static final int BATCH_SIZE = 50;

    private final MongoTemplate mongoTemplate;
    private MongoCursor<Document> cursor;
    private int batchCount = 0;

    @Override
    public void open(@Nonnull ExecutionContext executionContext) {
        try {
            Document queryDoc = buildQuery();
            Document fieldsDoc = buildProjection();

            this.cursor = mongoTemplate.getCollection("bids")
                    .find(queryDoc)
                    .projection(fieldsDoc)
                    .batchSize(BATCH_SIZE)
                    .noCursorTimeout(true)
                    .iterator();

            log.debug("[Reader] 공고 임베딩 Mongo Native 커서 열림 - 쿼리: {}", queryDoc.toJson());

        } catch (Exception e) {
            log.error("[Reader] 공고 임베딩 Mongo Native 커서 초기화 실패", e);
            throw new RuntimeException("NtceEmbeddingReader 커서 초기화 실패", e);
        }
    }

    @Override
    public List<NtceBase> read() {
        if (cursor == null || !cursor.hasNext()) return null;

        List<NtceBase> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;

        while (cursor.hasNext() && count++ < BATCH_SIZE) {
            Document doc = cursor.next();
            Long id = doc.getLong("_id");
            String name = doc.getString("bidNtceNm");

            if (id != null && name != null) {
                batch.add(new NtceBase(id, name));
            }
        }

        batchCount++;
        log.info("[Reader] 공고 임베딩 배치 #{} - {}건 반환", batchCount, batch.size());
        return batch.isEmpty() ? null : batch;
    }

    @Override
    public void close() {
        if (cursor != null) {
            try {
                cursor.close();
                log.info("[Reader] 공고 임베딩 커서 닫힘 - 총 {}건", batchCount);
                batchCount = 0;
            } catch (Exception e) {
                log.warn("[Reader] 공고 임베딩 커서 닫기 실패", e);
            }
        }
    }

    @Override
    public void update(@Nonnull ExecutionContext executionContext) {
    }

    private Document buildQuery(){
        return new Document("$and", Arrays.asList(
                new Document("bidNtceNm", new Document("$exists", true)),
                new Document("$or", Arrays.asList(
                        new Document("vector", new Document("$exists", false)),
                        new Document("keywords", new Document("$exists", false))
                )))
        );
    }

    private Document buildProjection() {
        return new Document()
                .append("_id", 1)
                .append("bidNtceNm", 1)
                ;
    }
}
