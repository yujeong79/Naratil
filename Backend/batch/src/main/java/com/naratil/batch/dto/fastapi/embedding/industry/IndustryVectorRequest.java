package com.naratil.batch.dto.fastapi.embedding.industry;

import com.naratil.batch.dto.fastapi.common.IndustryBase;
import lombok.Data;

import java.util.List;

/**
 * 1개 이상의 업종을 벡터 변환 요청
 */
@Data
public class IndustryVectorRequest {
    private List<IndustryBase> industries; // 벡터로 변환할 업종 정보 리스트
}
