package com.naratil.batch.controller;

import com.naratil.batch.service.CategoryCountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/batch")
@RequiredArgsConstructor
@Tag(name = "입찰공고 분류", description = "입찰공고 분류별 개수 세기 API")
public class BidCountController {

    private final CategoryCountService categoryCountService;

    @PostMapping("/bids/count")
    @Operation(summary="대분류별 공고 개수 계산", description="대분류별 공고 개수를 카운트하여 mongoDb에 저장")
    public ResponseEntity<?> countBidsByIndustrytyCd() {
        try {
            categoryCountService.countMajorCategory();
            return ResponseEntity.ok().body("입찰공고 초기화 작업 성공");
        } catch (Exception e) {
            log.error("입찰공고 개수 세기 작업 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("입찰공고 초기화 작업 실패: " + e.getMessage());
        }
    }
}
