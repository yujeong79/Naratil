package com.naratil.batch.client.fastapi;

import com.naratil.batch.dto.fastapi.common.IndustryBase;
import com.naratil.batch.dto.fastapi.embedding.industry.IndustryVectorRequest;
import com.naratil.batch.dto.fastapi.embedding.industry.IndustryVectorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndustryVectorApiClient {

    private final RestTemplate restTemplate;

    @Value("${fastapi.base-url}${fastapi.api.industry-vector}")
    private String industryVectorUrl;

    /**
     * 업종 리스트를 FastAPI에 전달하여 벡터값을 받아온다
     * @param industries 요청할 업종 목록
     * @return 벡터화된 업종 리스트
     */
    public IndustryVectorResponse getIndustryVectors(List<IndustryBase> industries) {
        IndustryVectorRequest request = new IndustryVectorRequest();
        request.setIndustries(industries);

        IndustryVectorResponse response = FastApiClientHelper.post(
                restTemplate,
                industryVectorUrl,
                request,
                IndustryVectorResponse.class
        );

        return response != null ? response : new IndustryVectorResponse();
    }
}

