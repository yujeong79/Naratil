package com.naratil.batch.writer;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.naratil.batch.model.BidNotice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 공고 벡터화 결과를 MongoDB에 Bulk Update하는 Writer
 * - bidNtceId 기준으로 vector, keywords 필드만 업데이트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NtceEmbeddingBulkWriter implements ItemWriter<List<BidNotice>> {

    private final MongoTemplate mongoTemplate;

    @Override
    public void write(Chunk<? extends List<BidNotice>> items) {
        log.debug("[Writer] 현재 공고 임베딩 시작 Chunk size: {}", items.size());

        for (List<BidNotice> batch : items) {
            if (batch == null || batch.isEmpty()) {
                log.debug("[Writer] 현재 공고 임베딩 빈 리스트 - 처리 건너뜀");
                continue;
            }

            log.debug("[Writer] 현재 공고 임베딩  MongoDB Bulk Update 대상 공고 수: {}", batch.size());

            List<WriteModel<Document>> updates = new ArrayList<>();

            for (BidNotice bid : batch) {
                // ID에 해당하는 문서 가져오기
                Document filter = new Document("_id", bid.getBidNtceId());

                // 필드
                Document updateFields = new Document()
                        .append("vector", bid.getVector())
                        .append("keywords", bid.getKeywords());

                // 문서
                Document update = new Document("$set", updateFields);

                updates.add(new UpdateOneModel<>(filter, update));
            }

            try {
                BulkWriteResult result = mongoTemplate.getDb()
                        .getCollection("bids") // 컬렉션 - BidNotice
                        .bulkWrite(updates, new BulkWriteOptions().ordered(false)); // 순서 상관없이

                log.info("[Writer] 현재 공고 임베딩 Bulk Update 완료 → matched: {}, modified: {}",
                        result.getMatchedCount(), result.getModifiedCount());
            } catch (Exception e) {
                log.error("[Writer] 현재 공고 임베딩  MongoDB Bulk Update 실패: {}", e.getMessage(), e);
                throw e;
            }
        }
    }

}
