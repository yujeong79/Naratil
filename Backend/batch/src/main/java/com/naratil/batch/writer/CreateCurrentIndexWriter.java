package com.naratil.batch.writer;

import com.naratil.batch.client.fastapi.UpdateIndexApiClient;
import com.naratil.batch.common.MongoCollectionChecker;
import com.naratil.batch.dto.fastapi.indexing.AddIndexRequest;
import com.naratil.batch.util.VectorUtils;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * M 현재 공고 인덱싱용 Writer
 * - 업종코드별로 분류
 * - 1000개씩 잘라서 FastAPI에 추가 호출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateCurrentIndexWriter implements ItemWriter<List<Document>> {

    private final MongoCollectionChecker collectionChecker;
    private final UpdateIndexApiClient apiClient;

    private static final int BATCH_SIZE = 100;

    @Override
    public void write(@Nonnull Chunk<? extends List<Document>> items) throws Exception {
        if (items.isEmpty()) {
            log.warn("[Writer] 인덱싱할 현재 공고가 없습니다.");
            return;
        }

        // items는 List<List<Document>> 이므로 풀어야 함
        List<Document> documents = new ArrayList<>();
        for (List<Document> batch : items) {
            documents.addAll(batch);
        }

        log.debug("[Writer] 현재 공고 인덱스 추가 시작 - 총 문서 수: {}", documents.size());

        Map<String, List<Document>> documentsByCollection = new HashMap<>();

        for (Document doc : documents) {
            String industryCode = doc.getString("indstrytyCd");
            String collectionName = collectionChecker.resolveIndustryCollection(industryCode);

            // 컬렉션별로 map으로 분할
            documentsByCollection
                    .computeIfAbsent(collectionName, k -> new ArrayList<>())
                    .add(doc);
        }

        for (Map.Entry<String, List<Document>> entry : documentsByCollection.entrySet()) {
            String collectionName = entry.getKey();
            List<Document> industryDocs = entry.getValue();

            log.debug("[Writer] 현재 공고 추가: 컬렉션 {} - 총 {}건 처리 시작", collectionName, industryDocs.size());

            for (int i = 0; i < industryDocs.size(); i += BATCH_SIZE) {
                List<Document> batch = industryDocs.subList(i, Math.min(i + BATCH_SIZE, industryDocs.size()));

                Map<Long, List<String>> keywordsMap = new HashMap<>();
                List<List<Float>> vectors = new ArrayList<>();
                List<Long> ids = new ArrayList<>();

                for (Document doc : batch) {
                    Long id = doc.getLong("_id");
                    keywordsMap.put(id, (List<String>)doc.get("keywords"));
                    vectors.add(VectorUtils.toFloatList(doc.get("vector")));
                    ids.add(id);
                }

                AddIndexRequest request = new AddIndexRequest();
                request.setCollectionName(collectionName);
                request.setKeywordsMap(keywordsMap);
                request.setVectors(vectors);
                request.setIds(ids);

                apiClient.addIndex(request, "current");

                log.info("[Writer] 현재 공고 인덱스 추가: 컬렉션 {} - {}건 인덱스 추가 완료", collectionName, ids.size());
            }
        }

        log.info("[Writer] 현재 공고 인덱스 추가 완료 - 컬렉션 {}개", documentsByCollection.size());
    }
}
