package com.naratil.bid.controller;

import com.naratil.bid.dto.BidDetailResponseDto;
import com.naratil.bid.service.BidNoticeDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("/api")
@RestController // 자동으로 객체가 "변수" : "값" 으로 매칭되어서 JSON 전환됨
@RequiredArgsConstructor
public class BidNoticeDetailController {

    private final BidNoticeDetailService bidNoticeDetailService;

    /**
     * 특정 공고 조회(현재)
     * @param bidId
     * @return
     */
    @GetMapping("/bids/{bidId}")
    public ResponseEntity<?> getNoticeById(@PathVariable long bidId) {    // 공고번호 X       공고id O
        log.info("getNoticeById, bidId: {}", bidId);
        BidDetailResponseDto bidDetailResponseDto = bidNoticeDetailService.getNoticeById(bidId);// 현재공고 및 과거유사공고3개
//        log.info("과거 유사 공고, bidDetailResponseDto: {}", bidDetailResponseDto.getPastData());
        return ResponseEntity.ok(bidDetailResponseDto);
    }

}



