package com.naratil.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 유사 공고 산출 조건
 */
@Data
@AllArgsConstructor
public class SimilarNtceCondition {
    private int cosineTopK; // 코사인 유사도 계산으로 산출할 개수
    private int jaccardTopK; // 자카드 유사도 계산으로 산출할 개수
    private float jaccardWeight; // Jaccard의 가중치 (0 ~ jaccard_weight)
    private float cosineWeight; // Cosine의 가중치 (-1 * cosine_weight ~ 1 * cosine_weight)
    private float cosineLowerBound; // Cosine 하한값
    private float lowerBound; // 유사 공고로 판단할 점수 하한값
    private int finalTopK; // 최종 산출할 개수
}