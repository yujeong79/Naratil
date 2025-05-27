package com.naratil.recommend.controller;

import com.naratil.bid.entity.BidNoticeMongoDB;
import com.naratil.recommend.service.RecommendService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api/recommend")
@RestController
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    @GetMapping
    public ResponseEntity<?> recommend(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer "))
            return ResponseEntity.badRequest().body("유효하지 않은 토큰 형식입니다.");

        List<BidNoticeMongoDB> recoBids = recommendService.recommend(authorization);
//        log.info("추천공고들 : {}", recoBids);
        return ResponseEntity.ok(recoBids);
    }

}
