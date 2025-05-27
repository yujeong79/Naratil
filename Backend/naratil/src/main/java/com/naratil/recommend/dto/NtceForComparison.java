package com.naratil.recommend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 공고끼리 비교를 위한 공고 정보
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NtceForComparison {
    private Long bidNtceId; // 공고 ID
    private String bidNtceNm; // 공고명
    private List<Float> vector = new ArrayList<>(); // 벡터
    private List<String> keywords = new ArrayList<>(); // 키워드
    private String collectionName; // 컬렉션이름
}
