package com.naratil.batch.reader;

import com.mongodb.client.MongoCursor;
import com.naratil.batch.dto.fastapi.common.NtceBase;
import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.item.*;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 과거 공고 키워드 추출 대상 Reader
 * - keywords 없는 것만
 */
@Slf4j
public class NtceKeywordExtractionReader implements ItemReader<List<NtceBase>>, ItemStream {

    private static final int BATCH_SIZE = 5_000;

    private final MongoTemplate mongoTemplate;
    private final String collectionName;
    private MongoCursor<Document> cursor;
    private int batchCount = 0;

    public NtceKeywordExtractionReader(MongoTemplate mongoTemplate, String collectionName) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public void open(@Nonnull ExecutionContext executionContext) {
        try {
            // keywords 필드가 없고 id/name은 있는 문서
            Document queryDoc = buildQuery();
            Document fieldsDoc = buildProjection();

            this.cursor = mongoTemplate.getCollection(collectionName)
                    .find(queryDoc)
                    .projection(fieldsDoc)
                    .batchSize(BATCH_SIZE)
                    .noCursorTimeout(true)
                    .iterator();

            log.debug("[Reader] 키워드 추출 커서 열림 - 컬렉션: {}, 쿼리: {}", collectionName, queryDoc.toJson());

        } catch (Exception e) {
            log.error("[Reader] 키워드 추출 Mongo 커서 초기화 실패 - 컬렉션: {}", collectionName, e);
            throw new RuntimeException("NtceKeywordExtractionReader 커서 초기화 실패", e);
        }
    }

    @Override
    public List<NtceBase> read() {
        if (cursor == null || !cursor.hasNext()) return null;

        List<NtceBase> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;

        while (cursor.hasNext() && count++ < BATCH_SIZE) {
            Document doc = cursor.next();

            Object bidNtceIdObj = doc.get("bidNtceId");
            String bidNtceNm = doc.getString("bidNtceNm");

            if (bidNtceIdObj instanceof Integer bidNtceId && bidNtceNm != null) {
                batch.add(new NtceBase(bidNtceId, bidNtceNm));
            }
        }

        batchCount++;
        log.info("[Reader] 키워드 추출 배치 #{} 완료 - {}건 반환 (컬렉션: {})", batchCount, batch.size(), collectionName);
        return batch.isEmpty() ? null : batch;
    }

    @Override
    public void close() {
        if (cursor != null) {
            try {
                cursor.close();
                log.info("[Reader] 키워드 추출 커서 닫힘 - 컬렉션: {}, 총 {}건", collectionName, batchCount);
                batchCount=0;
            } catch (Exception e) {
                log.warn("[Reader] 키워드 추출 커서 닫기 실패 - 컬렉션: {}", collectionName, e);
            }
        }
    }

    @Override
    public void update(@Nonnull ExecutionContext executionContext) {
    }

    private Document buildQuery(){
        return new Document("$and", Arrays.asList(
                new Document("bidNtceId", new Document("$exists", true)),
                new Document("bidNtceNm", new Document("$exists", true)),
                new Document("keywords", new Document("$exists", false))
        ));
    }

    private Document buildProjection() {
        return new Document()
                .append("_id", 1)
                .append("bidNtceId", 1)
                .append("bidNtceNm", 1);
    }
}