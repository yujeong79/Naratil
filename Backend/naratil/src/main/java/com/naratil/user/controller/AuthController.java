package com.naratil.user.controller;

import com.naratil.user.dto.logout.LogoutResponseDto;
import com.naratil.user.dto.login.LoginRequestDto;
import com.naratil.user.dto.login.LoginResponseDto;
import com.naratil.user.dto.signup.UserRequestDto;
import com.naratil.user.dto.signup.SignupResponseDto;
import com.naratil.user.service.AuthService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserRequestDto userRequestDto) {
        log.info("유저정보 : " + userRequestDto);

        boolean success = authService.signup(userRequestDto);
        String message = success ? "회원가입 성공" : "회원가입 실패";
        return ResponseEntity.ok(new SignupResponseDto(success, message));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        log.info("로그인에 들어옴");
        log.info("유저 이메일 : " + loginRequestDto.email());
        log.info("유저 비번 : " + loginRequestDto.password());


        try {
            LoginResponseDto loginResponseDto = authService.login(loginRequestDto); // 로그인 로직
            log.info("로그인 결과 : " + loginResponseDto.loginResult());
            return ResponseEntity.ok(loginResponseDto);// 로그인 결과 및 엑세스토큰 전달
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        // "Bearer " 접두사 제거 후 토큰 추출
        if (authorization == null || !authorization.startsWith("Bearer "))
            return ResponseEntity.badRequest().body("유효하지 않은 토큰 형식입니다.");

        authService.logout(authorization);// 토큰 무효화
        return ResponseEntity.ok(new LogoutResponseDto("로그아웃을 완료했습니다"));
    }
}
