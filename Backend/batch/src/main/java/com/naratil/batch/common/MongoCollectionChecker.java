package com.naratil.batch.common;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * MongoDB 컬렉션 목록
 * - 서버 시작 시 초기화
 * - 자정마다 최신 목록으로 갱신
 */
@Slf4j
@Component
public class MongoCollectionChecker {

    private final MongoTemplate mongoTemplate;

    @Getter
    private volatile Set<String> cachedCollections;

    public MongoCollectionChecker(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.cachedCollections = new HashSet<>(mongoTemplate.getCollectionNames());
        log.info("[MongoCollectionChecker] 초기 컬렉션 캐시: {}개 로딩됨", cachedCollections.size());
    }

    /**
     * 특정 이름의 컬렉션이 존재하는지
     * @param collectionName : 컬렉션 이름
     * @return 존재여부
     */
    public boolean exists(String collectionName) {
        return cachedCollections.contains(collectionName);
    }

    /**
     * 업종 코드로 컬렉션 이름 찾기
     * @param cd : 업종코드
     * @return 컬렉션 이름
     */
    public String resolveIndustryCollection(String cd) {
        if (cd == null || cd.isEmpty()) {
            return "industry_no_limit";
        }
        String collectionName = "industry_" + cd;
        return exists(collectionName) ? collectionName : "industry_etc";
    }

    /**
     * 자정마다 컬렉션 목록 갱신
     */
//    @Scheduled(cron = "0 10 0 * * *") // 매일 자정
    public void refreshCollectionCache() {
        try {
            Set<String> latest = new HashSet<>(mongoTemplate.getCollectionNames());
            this.cachedCollections = latest;
            log.info("[MongoCollectionChecker] 컬렉션 캐시 갱신 완료 - {}개 컬렉션", latest.size());
        } catch (Exception e) {
            log.error("[MongoCollectionChecker] 컬렉션 목록 갱신 실패", e);
        }
    }
}
