package com.naratil.recommend.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RecommendNtcesResponse {
    private Map<Long, List<RecommendedNtceScore>> recommendedNtces; // 기업별 추천목록
}
