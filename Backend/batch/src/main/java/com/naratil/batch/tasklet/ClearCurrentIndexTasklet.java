package com.naratil.batch.tasklet;

import com.naratil.batch.client.fastapi.ClearIndexApiClient;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * 현재 공고 인덱스 초기화 Tasklet
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClearCurrentIndexTasklet implements Tasklet {

    private final ClearIndexApiClient apiClient;

    @Override
    public RepeatStatus execute(@Nonnull StepContribution contribution, @Nonnull ChunkContext chunkContext) {
//        log.info("[Tasklet] 현재 공고 인덱스 초기화 시작");

        try {
            apiClient.clearFaissIndex("current");
            log.info("[Tasklet] 현재 공고 인덱스 초기화 성공");
        } catch (Exception e) {
            log.error("[Tasklet] 현재 공고 인덱스 초기화 실패", e);
            throw new RuntimeException("현재 공고 인덱스 초기화 실패", e);
        }

        return RepeatStatus.FINISHED;
    }
}
