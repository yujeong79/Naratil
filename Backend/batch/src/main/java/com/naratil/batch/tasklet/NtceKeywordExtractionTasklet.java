package com.naratil.batch.tasklet;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.Updates;
import com.naratil.batch.dto.fastapi.common.NtceBase;
import com.naratil.batch.reader.NtceKeywordExtractionReader;
import com.naratil.batch.util.KeywordExtractor;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class NtceKeywordExtractionTasklet implements Tasklet {

    private final MongoTemplate mongoTemplate;
    private final KeywordExtractor keywordExtractor;

    @Override
    public RepeatStatus execute(@Nonnull StepContribution contribution, @Nonnull ChunkContext chunkContext) {

        List<String> collections = new ArrayList<>(mongoTemplate.getCollectionNames())
                .stream()
                .filter(name -> name.startsWith("industry_"))
                .toList();

        log.debug("[Tasklet] 키워드 추출 대상 컬렉션 수: {}", collections.size());
        int allcount = 0;

        for (String collectionName : collections) {
            log.debug("[Tasklet] 키워드 추출 시작: {}", collectionName);

            try {
                // MongoCollection 직접 가져옴 (Low Level)
                MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);

                NtceKeywordExtractionReader reader = new NtceKeywordExtractionReader(mongoTemplate, collectionName);
                reader.open(new ExecutionContext());

                int totalUpdated = 0;

                while (true) {
                    List<NtceBase> batch = reader.read();
                    if (batch == null || batch.isEmpty()) break;

                    List<UpdateOneModel<Document>> updates = new ArrayList<>(batch.size());

                    for (NtceBase ntce : batch) {
                        List<String> keywords = keywordExtractor.extractCleanKeywords(ntce.getBidNtceNm());
                        updates.add(new UpdateOneModel<>(
                                Filters.eq("bidNtceId", ntce.getBidNtceId()),
                                Updates.set("keywords", keywords)
                        ));
                    }

                    if (!updates.isEmpty()) {
                        collection.bulkWrite(updates,new BulkWriteOptions().ordered(false));
                        totalUpdated += updates.size();
                    }
                }

                reader.close();
                allcount += totalUpdated;
                log.info("[Tasklet] {} 키워드 추출: {}건 키워드 업데이트 완료", collectionName, totalUpdated);

            } catch (Exception e) {
                log.error("[Tasklet] {} 키워드 추출 실패: {}", collectionName, e.getMessage(), e);
            }

            log.info("[Tasklet] 키워드 추출 종료: {}", collectionName);
        }

        log.info("[Tasklet] 전체 키워드 추출 Job 종료 - 총 {}건 업데이트", allcount);
        return RepeatStatus.FINISHED;
    }
}
