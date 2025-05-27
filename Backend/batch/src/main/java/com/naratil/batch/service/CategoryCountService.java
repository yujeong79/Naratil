package com.naratil.batch.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryCountService {

    private final JdbcTemplate jdbcTemplate;
    private final MongoTemplate mongoTemplate;

    public void countMajorCategory() {
        // mysql industry에서 대분류별 industry_code 조회
        for(int majorIndustryCode = 1; majorIndustryCode <= 10; majorIndustryCode++) {
            // 대분류별 업종코드 조회 sql 생성
            String sql = "SELECT industry_code FROM industry WHERE major_category_code = ?";
            List<Map<String, Object>> codes = jdbcTemplate.queryForList(sql, majorIndustryCode);

            // 1. 대분류별 업종코드 리스트 조회
            List<String> indstrytyCdList = codes.stream()
                .map(row -> ((Number) row.get("industry_code")).longValue())
                .map(this::zeroPadIndustryCode)
                .toList();

            log.info("indstrytyCdList : {}", indstrytyCdList);

            // 2. 입찰 공고 개수 카운트
            Query query = new Query(Criteria.where("indstrytyCd").in(indstrytyCdList));
            long totalCount = mongoTemplate.count(query, "bids");

            log.info("대분류 {} : {} 개", majorIndustryCode, totalCount);

            // 3. MongoDB bids_cnt 컬렉션에 저장 또는 업데이트
            Query upsertQuery = new Query(Criteria.where("major_category_code").is(majorIndustryCode));
            Update update = new Update().set("count", totalCount);
            mongoTemplate.upsert(upsertQuery, update, "bids_cnt");
        }
    }

    private String zeroPadIndustryCode(Long code) {
        return String.format("%04d", code); // 1 -> 0001
    }
}
