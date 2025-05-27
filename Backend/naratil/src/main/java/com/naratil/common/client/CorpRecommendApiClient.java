package com.naratil.common.client;

import com.naratil.recommend.dto.RecommendNtcesRequest;
import com.naratil.recommend.dto.RecommendNtcesResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 추천 공고 FastAPI 호출 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CorpRecommendApiClient {

    private final RestTemplate restTemplate;

    private static final String CORP_RECOMMEND_API_URL = "http://3.36.76.231:8000/batch/fastapi/recommend/ntces";

    /**
     * 기업의 추천 받아오기
     * @param request
     * @return
     */
    public RecommendNtcesResponse getRecommendNtces(RecommendNtcesRequest request) {
        RecommendNtcesResponse response = ApiClientHelper.post(
                restTemplate,
                CORP_RECOMMEND_API_URL,
                request,
                RecommendNtcesResponse.class
        );
        return response != null ? response : new RecommendNtcesResponse();
    }
}
