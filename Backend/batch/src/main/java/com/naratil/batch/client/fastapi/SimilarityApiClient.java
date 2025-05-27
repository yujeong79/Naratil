package com.naratil.batch.client.fastapi;

import com.naratil.batch.dto.fastapi.similarity.SimilarNtcesRequest;
import com.naratil.batch.dto.fastapi.similarity.SimilarNtcesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 유사 공고 FastAPI 호출 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityApiClient {

    private final RestTemplate restTemplate;

    @Value("${fastapi.base-url}${fastapi.api.ntce-similarity}")
    private String similarNtceUrl;

    public SimilarNtcesResponse getSimilarNtces(SimilarNtcesRequest request) {
        SimilarNtcesResponse response = FastApiClientHelper.post(
                restTemplate,
                similarNtceUrl,
                request,
                SimilarNtcesResponse.class
        );

        return response != null ? response : new SimilarNtcesResponse();
    }
}
