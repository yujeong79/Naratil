package com.naratil.bid.controller;

import com.naratil.bid.dto.BidCountResponseDto;
import com.naratil.bid.service.BidCountService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidCountController {

    private final BidCountService bidCountService;

    @GetMapping("/major-category/count")
    public ResponseEntity<?> getBidCountByMajorCategory() {
        try {
            List<BidCountResponseDto> bidCountResponseDtoList = bidCountService.getBidCountByMajorCategory();
            return ResponseEntity.ok(bidCountResponseDtoList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("분야별 공고 수 조회 중 오류가 발생했습니다.");
        }
    }
}
