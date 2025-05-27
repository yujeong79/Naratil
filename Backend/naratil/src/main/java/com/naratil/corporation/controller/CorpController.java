package com.naratil.corporation.controller;

import com.naratil.corporation.service.CorpService;
import com.naratil.corporation.dto.CorpRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/corp")
public class CorpController {

    private final CorpService corpService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CorpRequestDto corpRequestDto) {
        log.info("기업등록 컨트롤러 입성");
        if (authorization == null || !authorization.startsWith("Bearer "))
            return ResponseEntity.badRequest().body("유효하지 않은 토큰 형식입니다.");
        String token = authorization.substring(7);

//        log.info("기업등록 컨트롤러 입성");
        corpService.signup(corpRequestDto, token);
        return ResponseEntity.ok("기업 등록이 완료되었습니다.");
    }



}
