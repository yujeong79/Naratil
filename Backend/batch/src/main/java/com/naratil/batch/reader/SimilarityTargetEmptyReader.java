package com.naratil.batch.reader;

import com.mongodb.client.MongoCursor;
import com.naratil.batch.dto.fastapi.similarity.BidNtce;
import com.naratil.batch.model.BidNotice;
import com.naratil.batch.util.VectorUtils;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * M 현재공고 Reader
 * - vector, keywords, bidNtceNm 존재하는 공고만 대상
 * - MongoTemplate.getCollection 사용
 */
@Slf4j
@RequiredArgsConstructor
public class SimilarityTargetEmptyReader implements ItemReader<List<BidNtce>>, ItemStream {

    /**
     * Mongo DB (1200건 읽어옴) → Cursor
     * → read() 호출할 때마다 1개씩 꺼냄
     * → 200개 모이면 (chunk 만큼)
     * → Processor → Writer
     */
    private static final int BATCH_SIZE = 50;

    private final MongoTemplate mongoTemplate;
    private MongoCursor<Document> cursor;
    private int batchCount = 0;

    @Override
    public void open(@Nonnull ExecutionContext executionContext) {
        this.batchCount = 0;
        try {
            Document queryDoc = buildQuery();
            Document fieldsDoc = buildProjection();

            this.cursor = mongoTemplate.getCollection("bids")
                    .find(queryDoc)
                    .projection(fieldsDoc)
                    .batchSize(BATCH_SIZE)
                    .iterator();


            log.debug("[Reader] 유사 공고 커서 열림");

        } catch (Exception e) {
            log.error("[Reader] 유사 공고 커서 초기화 실패", e);
            throw new RuntimeException("SimilarityTargetReader 커서 초기화 실패", e);
        }
    }

    @Override
    public List<BidNtce> read() {
        if (cursor == null || !cursor.hasNext()) return null;

        List<BidNtce> batch = new ArrayList<>(BATCH_SIZE);
        int count = 0;

        while (cursor.hasNext() && count++ < BATCH_SIZE) {
            Document doc = cursor.next();

            BidNtce notice = new BidNtce();
            notice.setBidNtceId(doc.getLong("_id"));
            notice.setBidNtceNm(doc.getString("bidNtceNm"));
            notice.setVector(VectorUtils.toFloatList(doc.get("vector")));
            notice.setKeywords((List<String>) doc.get("keywords"));
            notice.setIndstrytyCd(doc.getString("indstrytyCd"));

            batch.add(notice);
        }

        batchCount++;
        log.info("[Reader] 유사 공고 배치 #{} - {}건 반환", batchCount, batch.size());

        return batch.isEmpty() ? null : batch;
    }

    @Override
    public void close() {
        if (cursor != null) {
            try {
                cursor.close();
                log.info("[유사 공고] 커서 닫힘 - 총 {}건", batchCount);
                batchCount = 0;
            } catch (Exception e) {
                log.warn("[유사 공고] 커서 닫기 실패", e);
            }
        }
    }

    @Override
    public void update(@Nonnull ExecutionContext executionContext) {
    }


    private Document buildQuery(){
        // bidNtceId는 _id(기본)
        return new Document("$and", Arrays.asList(
                new Document("vector", new Document("$exists", true)),
                new Document("keywords", new Document("$exists", true)),
                new Document("bidNtceNm", new Document("$exists", true)),
                new Document("similarNtces", new Document("$exists", true)),
                new Document("similarNtces", new Document("$size", 0))
        ));
    }

    private Document buildProjection() {
        return new Document()
                .append("_id", 1)
                .append("bidNtceNm", 1)
                .append("vector", 1)
                .append("keywords", 1)
                .append("bidNtceDt", 1)
//                .append("indstrytyCd", 1)
                ;
    }
}
