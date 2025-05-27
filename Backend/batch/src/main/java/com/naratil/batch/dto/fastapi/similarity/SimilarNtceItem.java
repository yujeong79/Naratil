package com.naratil.batch.dto.fastapi.similarity;

import lombok.Data;


/**
 * 유사 공고 정보
 */
@Data
public class SimilarNtceItem {
    private Long bidNtceId; // 공고 id
    private float cosineScore; // 코사인 유사도
    private float jaccardScore; // 자카드 유사도
    private float finalScore; // 최종 유사도 (정규화)

    private String collectionName; // 컬렉션 이름

}
