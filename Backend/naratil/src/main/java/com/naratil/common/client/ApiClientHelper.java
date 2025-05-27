package com.naratil.common.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class ApiClientHelper {

    public static <T, R> R post(RestTemplate restTemplate, String url, T requestBody, Class<R> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<T> entity = new HttpEntity<>(requestBody, headers);


            ResponseEntity<R> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    responseType
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("[ApiClientHelper] api 호출 실패: " + response.getStatusCode());
            }

            return response.getBody();

        } catch (Exception e) {
            log.error("[ApiClientHelper] 호출 실패 [{}]: {}", url, e.getMessage(), e);
            throw new RuntimeException("[ApiClientHelper] api 호출 중 오류", e);
        }
    }

}
