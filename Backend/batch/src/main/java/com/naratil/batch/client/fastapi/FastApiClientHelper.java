package com.naratil.batch.client.fastapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Slf4j
public class FastApiClientHelper {

    public static <T, R> R post(RestTemplate restTemplate, String url, T requestBody, Class<R> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);

            // 직렬화된 요청 로그
//            log.debug("FastAPI 요청 JSON → {}", objectMapper.writeValueAsString(requestBody));

            ResponseEntity<R> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    responseType
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("FastAPI 호출 실패: " + response.getStatusCode());
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("FastAPI 호출 실패 [{}]: {}", url, e.getMessage(), e);
            throw new RuntimeException("FastAPI 호출 중 오류", e);
        }
    }

}
