package com.naratil.batch.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 요청을 위한 RestTemplate 설정 클래스 외부 API와 통신하기 위한 RestTemplate Bean을 제공
 */
@Configuration

public class RestTemplateConfig {

    /**
     * RestTemplate Bean 생성
     */
    @Bean
    public RestTemplate restTemplate() {
        // 기본 HTTP 요청 팩토리 생성 및 타임아웃 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);    // 연결 타임아웃: 30초
        factory.setReadTimeout(50000);  // 읽기 타임아웃: 50초

        return new RestTemplateBuilder()
            // BufferingClientHttpRequestFactory를 사용하여 요청/응답 본문을 여러 번 읽을 수 있도록 함
            // 로깅이나 디버깅 목적으로 요청/응답 본문을 여러 번 읽어야 할 때 유용
            .requestFactory(() -> new BufferingClientHttpRequestFactory(factory))
            .build();
    }
}
