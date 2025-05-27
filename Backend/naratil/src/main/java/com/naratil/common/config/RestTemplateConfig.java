package com.naratil.common.config;

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
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(50000);

        return new RestTemplateBuilder()
            .requestFactory(() -> new BufferingClientHttpRequestFactory(factory))
            .build();
    }
}
