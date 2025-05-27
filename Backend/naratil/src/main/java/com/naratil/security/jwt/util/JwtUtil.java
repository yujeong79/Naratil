package com.naratil.security.jwt.util;

import com.naratil.user.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${jwt.access-token-secret}")
    private String accessTokenSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    // 액세스 토큰 생성 (Bearer 접두어 포함)
    public String generateAccessToken(User user) {
        SecretKey secretKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes());

        String token = Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        return "Bearer " + token;
    }

    // JWT 토큰에서 사용자 ID 추출
    public String getUserIdFromJwtToken(String token) {
        try {
            SecretKey secretKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes());

            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // 👈 ID를 String으로 저장했으니
        } catch (Exception e) {
            throw new JwtException("JWT Token is invalid or expired.");
        }
    }

    // JWT 토큰 유효성 검증
    public boolean validateJwtToken(String token) {
        try {
            // 비밀 키 생성
            SecretKey secretKey = Keys.hmacShaKeyFor(accessTokenSecret.getBytes());

            Jwts.parserBuilder()
                    .setSigningKey(secretKey)  // 비밀 키 설정
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // 토큰이 유효하지 않으면 false 반환
        }
    }
}
