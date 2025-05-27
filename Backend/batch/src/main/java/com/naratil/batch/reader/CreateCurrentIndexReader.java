package com.naratil.batch.reader;

import com.mongodb.client.MongoCursor;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * M 현재 공고 인덱싱용 Reader
 * - 1000개씩 읽고 5000개 단위로 반환
 */
@Slf4j
@RequiredArgsConstructor
public class CreateCurrentIndexReader implements ItemReader<List<Document>>, ItemStream {

    private final MongoTemplate mongoTemplate;

    private static final int CURSOR_BATCH_SIZE = 500;
    private static final int CHUNK_SIZE = 500;

    private MongoCursor<Document> cursor;
    private boolean initialized = false;
    private int readCount = 0;

    @Override
    public void open(@Nonnull ExecutionContext executionContext) {
        try {
            this.cursor = mongoTemplate.getCollection("bids")
                    .find(buildQuery())
                    .projection(buildProjection())
                    .sort(new Document("indstrytyCd", 1))  // 업종코드 기준 정렬
                    .batchSize(CURSOR_BATCH_SIZE)
                    .iterator();
            initialized = true;

            log.debug("[Reader] 현재 공고 인덱싱 Mongo Native 커서 열림 - 쿼리: {}", buildQuery().toJson());
        } catch (Exception e) {
            log.error("[Reader] 현재 공고 인덱싱 Mongo Native 커서 초기화 실패", e);
            throw new RuntimeException("CurrentIndexReader 커서 초기화 실패", e);
        }
    }

    @Override
    public List<Document> read() {
        if (!initialized) return null;
        List<Document> documents = new ArrayList<>();

        while (cursor.hasNext() && documents.size() < CHUNK_SIZE) {
            documents.add(cursor.next());
            readCount++;
        }

        if (documents.isEmpty()) {
            return null;
        }

        log.info("[Reader] 현재 공고 인덱싱 - {}건 읽어옴 (총 누적: {}건)", documents.size(), readCount);
        return documents;
    }

    @Override
    public void close() {
        if (cursor != null) {
            try {
                cursor.close();
                log.info("[Reader] 현재 공고 인덱싱 커서 닫힘 - 총 읽은 건수: {}", readCount);
                readCount=0;
                initialized = false;
            } catch (Exception e) {
                log.warn("[Reader] 현재 공고 인덱싱 커서 닫기 실패", e);
            }
        }
    }

    @Override
    public void update(@Nonnull ExecutionContext executionContext) {
    }

    private Document buildQuery() {
        return new Document("vector", new Document("$exists", true))
                .append("keywords", new Document("$exists", true));

    }

    private Document buildProjection() {
        return new Document()
                .append("_id", 1)
                .append("vector", 1)
                .append("keywords", 1)
                .append("indstrytyCd", 1)
                ;
    }
}
