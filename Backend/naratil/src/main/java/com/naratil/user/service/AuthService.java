package com.naratil.user.service;

import com.naratil.user.dto.login.LoginRequestDto;
import com.naratil.user.dto.login.LoginResponseDto;
import com.naratil.user.dto.signup.UserRequestDto;
import com.naratil.security.jwt.service.JwtService;
import com.naratil.security.jwt.util.JwtUtil;
import com.naratil.user.entity.User;
import com.naratil.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;

    // 회원가입 로직
    @Transactional  // DB의 Insert 작업에 해당하므로 꼭 필요
    public boolean signup(UserRequestDto userRequestDto) {

        String hashedPassword = passwordEncoder.encode(userRequestDto.password()); // 비번 해싱 적용

        User user = User.createUserWithCorp(userRequestDto, hashedPassword);    // 정적 팩토리 패턴
        User userId = userRepository.save(user);      // 유저 데이터 저장 및 유저객체 반환

        log.info("user 저장 완료, userId: {}", userId.getId());
        return true;
    }

    // 로그인 로직 (JWT 토큰 생성 추가)
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        log.info("login request: {}", loginRequestDto);

        User user = userRepository.findByEmail(loginRequestDto.email())
            .orElse(null);

        if (user == null || !passwordEncoder.matches(loginRequestDto.password(), user.getPassword())) {
            throw new BadCredentialsException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        return new LoginResponseDto(true, accessToken);
    }

    // JWT 로그아웃 처리
    public void logout(String authorization) {
        String token = authorization.substring(7); // "Bearer " 이후의 토큰 값만 추출
        jwtService.invalidateAccessToken(token); // 토큰 무효화 로직 (예: 블랙리스트에 추가, Redis 활용 등)
        log.info("로그아웃 처리 완료, 토큰 무효화됨: {}", token);
    }
}
