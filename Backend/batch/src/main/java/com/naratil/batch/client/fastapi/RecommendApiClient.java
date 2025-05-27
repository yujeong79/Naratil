package com.naratil.batch.client.fastapi;

import com.naratil.batch.dto.fastapi.recommend.RecommendNtcesRequest;
import com.naratil.batch.dto.fastapi.recommend.RecommendNtcesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 추천 공고 FastAPI 호출 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendApiClient {

    private final RestTemplate restTemplate;

    @Value("${fastapi.base-url}${fastapi.api.ntce-recommend}")
    private String recommendNtceUrl;

    public RecommendNtcesResponse getRecommendNtces(RecommendNtcesRequest request) {
        RecommendNtcesResponse response = FastApiClientHelper.post(
                restTemplate,
                recommendNtceUrl,
                request,
                RecommendNtcesResponse.class
        );

        return response != null ? response : new RecommendNtcesResponse();
    }
}
