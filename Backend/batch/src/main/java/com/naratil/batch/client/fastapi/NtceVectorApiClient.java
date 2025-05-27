package com.naratil.batch.client.fastapi;

import com.naratil.batch.dto.fastapi.common.NtceBase;
import com.naratil.batch.dto.fastapi.embedding.notice.NtceVectorRequest;
import com.naratil.batch.dto.fastapi.embedding.notice.NtceVectorResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NtceVectorApiClient {

    private final RestTemplate restTemplate;

    @Value("${fastapi.base-url}${fastapi.api.ntce-vector}")
    private String ntceVectorUrl;

    /**
     * 공고 리스트를 FastAPI에 전달하여 벡터값을 받아온다
     * @param ntces 요청할 공고 목록
     * @return 벡터화된 공고 리스트
     */
    public NtceVectorResponse getNtceVectors(List<NtceBase> ntces) {
        NtceVectorRequest request = new NtceVectorRequest();
        request.setNtces(ntces);

        NtceVectorResponse response = FastApiClientHelper.post(
                restTemplate,
                ntceVectorUrl,
                request,
                NtceVectorResponse.class
        );

        return response != null ? response : new NtceVectorResponse();
    }
}
