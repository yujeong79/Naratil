package com.naratil.security.jwt.service;

import com.naratil.security.jwt.util.JwtUtil;
import com.naratil.user.entity.User;
import com.naratil.user.repository.UserRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final Set<String> blacklistedTokens = new HashSet<>();  // 블랙리스트를 메모리로 관리 (Redis 등 외부 시스템 사용 가능)

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // ✅ 로그인 시 JWT 발급
    public String generateAccessToken(User user) {
        // 액세스 토큰만 생성
        String accessToken = jwtUtil.generateAccessToken(user);

        // 액세스 토큰만 반환
        return accessToken;
    }

    // ✅ 액세스 토큰 검증
    public void validateAccessToken(String authorizationHeader) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("인증 토큰이 없습니다.");
        }
        String token = authorizationHeader.substring(7);
        boolean valid = jwtUtil.validateJwtToken(token);
        if (!valid) {
            throw new IllegalArgumentException("유효하지 않은 액세스 토큰입니다.");
        }
        log.info("엑세스 토큰 유효함!");
    }

    // ✅ 로그아웃 (리프레시 토큰 제거, 액세스 토큰과 관련 없음)
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // 리프레시 토큰을 사용하지 않으므로 추가 작업은 필요 없음
        userRepository.save(user);
    }

    public void invalidateAccessToken(String accessToken) {
        blacklistedTokens.add(accessToken);  // 토큰을 블랙리스트에 추가
        log.info("토큰이 블랙리스트에 추가되었습니다: {}", accessToken);
    }
    

    // ✅ 유저 아이디 추출
    public Long getUserIdFromJwtToken(String token) {
        return Long.valueOf(jwtUtil.getUserIdFromJwtToken(token));
    }


}