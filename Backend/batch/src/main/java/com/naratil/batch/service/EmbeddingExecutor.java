package com.naratil.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * 임베딩 병렬 실행을 담당하는 Executor
 */
@Slf4j
@Component
@RequiredArgsConstructor
public abstract class EmbeddingExecutor<T, R> {

    // 변경: 250 *2 순차 실행
    private static final int MAX_CALL_SIZE = 250;   // 한 번 FastAPI 호출당 250
    private static final int MAX_PARALLEL = 1;      // 동시에 5개 병렬 호출

    private final Executor embeddingTaskExecutor;

    /**
     * 리스트를 병렬로 FastAPI에 요청하고 결과를 모은다.
     *
     * @param inputList 요청할 입력 데이터
     * @param apiCall FastAPI 호출 함수
     * @return 응답 리스트
     */
    protected List<R> executeInParallel(List<T> inputList, Function<List<T>, List<R>> apiCall) {
        List<List<T>> chunks = ListUtils.partition(inputList, MAX_CALL_SIZE);
        log.debug("[임베딩] 전체 {}개 → {}개 청크 (청크당 {}개)", inputList.size(), chunks.size(), MAX_CALL_SIZE);

        List<CompletableFuture<List<R>>> futures = new ArrayList<>();

        for (List<T> chunk : chunks) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    log.debug("[임베딩] FastAPI 호출 시작 - 청크 크기: {}", chunk.size());
                    return apiCall.apply(chunk);
                } catch (Exception e) {
                    log.error("[임베딩] FastAPI 호출 실패 - 이 청크는 무시", e);
                    return List.of();
                }
            }, embeddingTaskExecutor));
        }

        List<R> result = new ArrayList<>();
        int completed = 0;

        for (CompletableFuture<List<R>> future : futures) {
            try {
                result.addAll(future.get());
                completed++;
                if (completed % MAX_PARALLEL == 0) {
                    log.debug("[임베딩] {}개 청크 완료 (병렬 제한: {}개)", completed, MAX_PARALLEL);
                }
            } catch (Exception e) {
                log.error("[임베딩] 병렬 호출 중 Future 실패", e);
            }
        }

        log.debug("[임베딩] 병렬 처리 전체 완료 - 총 {}건 성공", result.size());
        return result;
    }
}
