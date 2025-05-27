package com.naratil.batch.client.fastapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClearIndexApiClient {

    private final RestTemplate restTemplate;

    @Value("${fastapi.base-url}/indexing/current/clear")
    private String currentIndexingUrl;

    @Value("${fastapi.base-url}/indexing/past/clear")
    private String pastIndexingUrl;

    /**
     * Faiss에 Indexing
     * @param type 요청 타입 (url 구분)
     * @return 단순 메세지
     */
    public String clearFaissIndex(String type) {
        String url = "";
        if (type.equals("current")) url = currentIndexingUrl;
        else if (type.equals("past")) url = pastIndexingUrl;

        return  FastApiClientHelper.post(
                restTemplate,
                url,
                null,
                String.class
        );
    }
}

