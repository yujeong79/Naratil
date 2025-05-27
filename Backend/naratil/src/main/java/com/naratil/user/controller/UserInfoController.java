package com.naratil.user.controller;

import com.naratil.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    /**
     * 로그인된 사용자의 정보를 반환하는 컨트롤러
     * http 요청 header의 쿠키에서 token을 가져와 사용자 정보를 조회한다
     * @param authorization
     * @return 사용자 정보 : 이메일, 이름, 전화번호, 사업자번호, 회사명, 대표명, 개업일, 종업원수
     */
    @GetMapping("/info")
    public ResponseEntity<?> getLoginUser(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            return ResponseEntity.badRequest().body("유효하지 않은 토큰 형식입니다.");

        String token = authorization.substring(7);

        return ResponseEntity.ok(userInfoService.getUserInfo(token));
    }


}
