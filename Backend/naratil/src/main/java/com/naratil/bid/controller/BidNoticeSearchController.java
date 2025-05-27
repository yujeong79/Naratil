package com.naratil.bid.controller;

import com.naratil.bid.dto.BidSearchRequestDto;
import com.naratil.bid.entity.BidNoticeMongoDB;
import com.naratil.bid.service.BidNoticeSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/api/bids")
@RestController // 자동으로 객체가 "변수" : "값" 으로 매칭되어서 JSON 전환됨
@RequiredArgsConstructor
public class BidNoticeSearchController {

    private final BidNoticeSearchService bidNoticeSearchService;

    @GetMapping("/search")
    public ResponseEntity<List<Document>> searchBids(@ModelAttribute BidSearchRequestDto bidSearchRequestDto) {
//        log.info("검색필터 전");
        List<Document> bids = bidNoticeSearchService.searchBids(bidSearchRequestDto);
//        log.info("검색필터 후");

        return ResponseEntity.ok(bids);
    }
}
