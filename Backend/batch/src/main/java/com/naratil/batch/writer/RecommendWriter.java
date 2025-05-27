package com.naratil.batch.writer;

import com.naratil.batch.model.Recommend;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 추천 공고 Writer
 * - 추천 결과를 recommend 테이블에 Bulk Insert
 * - 데이터가 많을 경우 일정 단위로 분할하여 Insert 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendWriter implements ItemWriter<List<Recommend>> {

    private static final String INSERT_SQL_PREFIX = """
        INSERT INTO recommend (corpid, bid_ntce_id, score) VALUES
    """;

    private static final int BATCH_INSERT_SIZE = 50;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void write(@Nonnull Chunk<? extends List<Recommend>> chunk) {
        List<Recommend> recommends = chunk.getItems().stream()
                .flatMap(List::stream)
                .toList();

        if (recommends.isEmpty()) {
            log.warn("[Writer] 추천 데이터 없음 (스킵)");
            return;
        }

        log.debug("[Writer] 추천 데이터 총 {}건 Insert 시작", recommends.size());

        try {
            List<List<Recommend>> partitioned = partition(recommends, BATCH_INSERT_SIZE);

            int count = 0;

            for (List<Recommend> part : partitioned) {
                count += insertBatch(part);
            }

            log.info("[Writer] 추천 데이터 Insert 완료 - 총 {}/{}건", count , recommends.size());
        } catch (Exception e) {
            log.error("[Writer] 추천 데이터 Insert 실패", e);
            throw new RuntimeException("추천 데이터 Insert 실패", e);
        }
    }

    /**
     * 리스트를 일정 크기로 분할
     */
    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    /**
     * Recommend 리스트를 Multi-Row Insert로 DB에 저장
     */
    private int insertBatch(List<Recommend> recommends) {
        StringBuilder sql = new StringBuilder(INSERT_SQL_PREFIX);
        List<Object> params = new ArrayList<>();

        for (int i = 0; i < recommends.size(); i++) {
            sql.append("(?, ?, ?)");
            if (i < recommends.size() - 1) {
                sql.append(", ");
            }
            Recommend r = recommends.get(i);
            params.add(r.getCorpId());
            params.add(r.getBidNtceId());
            params.add(r.getScore());
        }
        return jdbcTemplate.update(sql.toString(), params.toArray());
    }
}
