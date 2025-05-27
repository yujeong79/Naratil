package com.naratil.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보안 비활성화 (API 사용 시 필요)

                .authorizeHttpRequests(auth -> auth
                        // ===== ✅ 운영 환경 (회원가입, 로그인만 공개) =====
                        //.requestMatchers("/api/signup", "/api/login").permitAll() // 회원가입, 로그인 API만 공개
                        //.anyRequest().authenticated() // 그 외 요청은 인증 필요

                        // ===== ✅ 테스트 환경 (모든 API 공개) =====
                        .anyRequest().permitAll() // 개발/테스트 시 모든 API 공개
                )

                .formLogin(form -> form.disable()) // 기본 로그인 화면 비활성화
                .httpBasic(basic -> basic.disable()); // HTTP Basic 인증 비활성화

        return http.build();
    }
}


