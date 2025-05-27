package com.naratil.batch.dto.fastapi.recommend;

import lombok.Data;

/**
 * 각 과거 낙찰 공고와의 유사도 평균 / 랭킹 횟수 
 */
@Data
public class RecommendedNtceScore {
    private long bidNtceId; // 공고 ID
    private float totalScore;  // 유사도 합계
    private float avgScore; // 평균
    private float adjustScore; // 조정 점수
    private int count; // 각 공고마다 top-k에 해당 공고가 랭크된 횟수
}
