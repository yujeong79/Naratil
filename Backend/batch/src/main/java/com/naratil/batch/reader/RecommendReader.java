package com.naratil.batch.reader;

import com.naratil.batch.model.Corporation;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 추천 공고용 기업 Reader
 * - DB에서 500개씩 읽어와서
 * - 커서 기반으로 이어서 읽기 (OFFSET 사용 안 함)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendReader implements ItemReader<List<Corporation>>, ItemStream {

    private static final int FETCH_SIZE = 50;

    @Qualifier("businessJdbcTemplate")
    private final JdbcTemplate jdbcTemplate;

    private List<Corporation> buffer = new ArrayList<>();
    private boolean initialized = false;
    private int batchCount = 0;
    private long lastCorpId = 0L;

    private static final String SQL = """
        SELECT corpid, bizno, emplye_num, industry_code
        FROM corporation c
        LEFT OUTER JOIN industry i ON c.industry_id = i.id
        WHERE c.corpid > ?
        ORDER BY c.corpid
        LIMIT ?
    """;

    @Override
    public void open(@Nonnull ExecutionContext executionContext) {
        log.debug("[Reader] 추천 기업 Reader 커서 열기 시작");
        if (executionContext.containsKey("lastCorpId")) {
            this.lastCorpId = executionContext.getLong("lastCorpId");
            log.debug("[Reader] 이전 실행의 lastCorpId 복구: {}", lastCorpId);
        } else {
            this.lastCorpId = 0L;
        }
        initialized = true;
    }

    @Override
    public List<Corporation> read() {
        if (!initialized) {
            log.error("[Reader] 추천 커서 초기화 실패");
            throw new IllegalStateException("RecommendReader 커서 초기화 실패");
        }

        log.debug("[Reader] 추천 기업 커서 열림");

        buffer = fetchNextBatch();
        if (buffer.isEmpty()) {
            log.debug("[Reader] 추천 기업 읽기 완료.");
            return null;
        }

        List<Corporation> batch = new ArrayList<>(buffer);
        buffer.clear();
        batchCount += batch.size();
        log.info("[Reader] 추천 기업 #{} - {}개 반환", batchCount, batch.size());
        return batch;
    }

    @Override
    public void close() {
        log.info("[Reader] 추천 기업 커서 닫힘 - 총 {}건", batchCount);
        buffer.clear();
        lastCorpId = 0L;
        batchCount=0;
        initialized = false;
    }

    @Override
    public void update(@Nonnull ExecutionContext executionContext) {
        executionContext.putLong("lastCorpId", this.lastCorpId);
    }

    /**
     * DB에서 다음 batch를 읽어옴
     */
    private List<Corporation> fetchNextBatch() {
        List<Corporation> corporations = jdbcTemplate.query(
                SQL,
                new Object[]{lastCorpId, FETCH_SIZE},
                (rs, rowNum) -> {
                    Corporation corp = new Corporation();
                    corp.setCorpId(rs.getLong("corpid"));
                    corp.setBizno(rs.getString("bizno"));
                    corp.setEmpNum(rs.getInt("emplye_num"));
                    corp.setIndustryCode(rs.getString("industry_code"));
                    return corp;
                }
        );

        if (!corporations.isEmpty()) {
            this.lastCorpId = corporations.get(corporations.size() - 1).getCorpId();
        }

        log.debug("[Reader] 총 {}개 기업 읽음 (lastCorpId: {})", corporations.size(), lastCorpId);
        return corporations;
    }
}
