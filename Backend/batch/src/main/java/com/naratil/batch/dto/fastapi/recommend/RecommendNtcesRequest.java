package com.naratil.batch.dto.fastapi.recommend;

import com.naratil.batch.dto.fastapi.common.NtceForComparison;
import com.naratil.batch.dto.fastapi.common.SimilarNtceCondition;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 과거 낙찰 공고들과, 현재 진행중인 공고들을 비교하여 상위 랭크에 있는 공고들을 요청
 */
@Data
public class RecommendNtcesRequest {
    private Map<Long, List<NtceForComparison>> corpNtces; // 기업마다 과거 낙찰 기록
    private SimilarNtceCondition similarNtceCondition; // 산출 조건
}
