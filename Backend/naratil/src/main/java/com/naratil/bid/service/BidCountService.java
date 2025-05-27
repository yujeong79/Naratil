package com.naratil.bid.service;

import com.naratil.bid.dto.BidCountResponseDto;
import com.naratil.bid.entity.BidCategoryCount;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidCountService {

    private final MongoTemplate mongoTemplate;

    public List<BidCountResponseDto> getBidCountByMajorCategory() {
        try {
            List<BidCategoryCount> documents = mongoTemplate.findAll(BidCategoryCount.class,
                "bids_cnt");

            return documents.stream()
                .map(doc -> new BidCountResponseDto(doc.getMajorCategoryCode(), doc.getCount()))
                .toList();
        } catch (Exception e) {
            log.error("분야별 공고 수 조회 실패", e);
            throw new RuntimeException("분야별 공고 수 조회 중 오류가 발생했습니다.");
        }
    }
}
