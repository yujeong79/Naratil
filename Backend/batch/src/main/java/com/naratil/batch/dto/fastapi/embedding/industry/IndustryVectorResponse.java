package com.naratil.batch.dto.fastapi.embedding.industry;

import lombok.Data;

import java.util.List;

/**
 * 업종 벡터 변환 요청에 대한 응답
 */
@Data
public class IndustryVectorResponse {
    private List<IndustryVector> success;
    private List<IndustryVectorFail> failed;

    /**
     * 벡터화 실패한 업종 정보
     */
    @Data
    public static class IndustryVectorFail {
        private long industryId;
        private String industryNm;
        private String reason;
    }
}