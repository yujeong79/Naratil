package com.naratil.batch.service;

import com.naratil.batch.client.fastapi.NtceVectorApiClient;
import com.naratil.batch.dto.fastapi.common.NtceBase;
import com.naratil.batch.dto.fastapi.embedding.notice.NtceVector;
import com.naratil.batch.dto.fastapi.embedding.notice.NtceVectorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * 공고 벡터 임베딩 처리 서비스
 */
@Slf4j
@Service
public class NtceEmbeddingService extends EmbeddingExecutor<NtceBase, NtceVector> {

    public NtceEmbeddingService(@Qualifier("embeddingTaskExecutor") Executor executor,
                                NtceVectorApiClient apiClient) {
        super(executor);
        this.apiClient = apiClient;
    }

    private final NtceVectorApiClient apiClient;
    private static final int MAX_BATCH_SIZE = 250; // 조정

    /**
     * 공고 벡터화 실행
     *
     * @param ntces 공고 입력
     * @return FastAPI 응답 (성공/실패 분리된 구조)
     */
    public NtceVectorResponse embed(List<NtceBase> ntces) {
        if (ntces.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("요청은 최대 " + MAX_BATCH_SIZE + "개까지 가능합니다. 현재: " + ntces.size());
        }

        log.debug("[임베딩 시작] 전체 공고 수: {}", ntces.size());

        // 병렬 요청 실행 (성공한 응답만 수집)
        List<NtceVector> successList = executeInParallel(
                ntces,
                chunk -> apiClient.getNtceVectors(chunk).getSuccess()
        );

        NtceVectorResponse response = new NtceVectorResponse();
        response.setSuccess(successList);

        log.debug("[임베딩 완료] 성공 {}건", successList.size());
        return response;
    }
}
