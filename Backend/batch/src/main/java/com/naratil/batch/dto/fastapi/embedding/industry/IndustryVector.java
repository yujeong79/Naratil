package com.naratil.batch.dto.fastapi.embedding.industry;

import lombok.Data;

import java.util.List;

/**
 * 업종 벡터
 */
@Data
public class IndustryVector {
    private int industryId; // 업종 ID
    private String industryNm; // 업종명
    private List<Float> vector; // 업종명 벡터
}
