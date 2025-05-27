package com.naratil.batch.dto.fastapi.similarity;

import com.naratil.batch.dto.fastapi.common.NtceForComparison;
import com.naratil.batch.dto.fastapi.common.SimilarNtceCondition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 특정 현재 공고와 유사한 과거 공고 리스트 요청
 */
@Data
@AllArgsConstructor
public class SimilarNtcesRequest {
    private List<NtceForComparison> currentNtces; // 특정 현재 공고 정보
    private SimilarNtceCondition similarNtceCondition; // 유사 공고 산출 조건

}
