package com.naratil.batch.writer;

import com.naratil.batch.model.BidNotice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * 입찰공고 데이터를 MongoDB에 저장하는 클래스
 */
@Slf4j
public class BidNoticeMongoItemWriter implements ItemWriter<BidNotice> {

    // 실제 MongoDB 저장 작읍을 수행할 위임 작성기
    private final ItemWriter<BidNotice> delegateWriter;

    public BidNoticeMongoItemWriter(MongoTemplate mongoTemplate, String collectionName) {
        // MongoItemWriterBuilder를 사용하여 위임 작성기 생성
        this.delegateWriter = new MongoItemWriterBuilder<BidNotice>()
            .template(mongoTemplate)
            .collection(collectionName)
            .build();
    }

    /**
     * 청크 단위로 묶인 BidNotice 객체들을 MongoDB에 저장
     * @param chunk 저장할 BidNotice 객체 모음
     */
    @Override
    public void write(Chunk<? extends BidNotice> chunk) throws Exception {
        if (!chunk.isEmpty()) {
            log.debug("🐛 MongoDB에 {} 개의 BidNotice 저장 시작", chunk.size());
        }

        try {
            // 위임 작성기를 사용하여 MongoDB에 데이터 저장
            delegateWriter.write(chunk);
            log.debug("🐛 MongoDB에 {} 개의 BidNotice 저장 성공", chunk.size());
        } catch (Exception e) {
            log.error("🐛 MongoDB에 {} 개의 BidNotice 저장 실패 : {}", chunk.size(), e.getMessage());
            throw e;
        }
    }
}
