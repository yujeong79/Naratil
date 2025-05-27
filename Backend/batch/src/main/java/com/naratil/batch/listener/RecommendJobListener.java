package com.naratil.batch.listener;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 추천 공고 Job 시작시 recommend 테이블 초기화
 */
@Slf4j
@Component
public class RecommendJobListener implements JobExecutionListener {

    private final JdbcTemplate jdbcTemplate;

    public RecommendJobListener(@Qualifier("businessDataSource") DataSource businessDataSource) {
        this.jdbcTemplate = new JdbcTemplate(businessDataSource);
    }

    @Override
    public void beforeJob(@Nonnull JobExecution jobExecution) {
        log.info("[Listener] 추천 공고 테이블 초기화 시작");

        try {
            int deleted = jdbcTemplate.update("DELETE FROM recommend");
            log.info("[Listener] 추천 공고 테이블 초기화 완료 - 삭제 건수: {}", deleted);
        } catch (Exception e) {
            log.error("[Listener] 추천 공고 테이블 초기화 실패", e);
            throw new RuntimeException("추천 테이블 초기화 실패", e);
        }
    }

    @Override
    public void afterJob(@Nonnull JobExecution jobExecution) {
        log.debug("[Listener] 추천 공고 배치 작업 완료 - 상태: {}", jobExecution.getStatus());
    }
}
