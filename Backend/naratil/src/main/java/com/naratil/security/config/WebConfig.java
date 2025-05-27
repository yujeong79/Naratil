package com.naratil.security.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 모든 경로에 대해 CORS를 허용
        registry.addMapping("/**")
                .allowedOrigins("https://j12a506.p.ssafy.io") // 허용할 도메인 (프론트엔드 서버 URL)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 허용할 HTTP 헤더
                .allowCredentials(true) // 쿠키 및 인증 정보 허용
                .maxAge(3600); // preplate 재요청 시간 1시간 = 3600초
    }
}
