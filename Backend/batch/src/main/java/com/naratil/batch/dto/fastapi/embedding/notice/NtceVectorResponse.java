package com.naratil.batch.dto.fastapi.embedding.notice;

import lombok.Data;

import java.util.List;

/**
 * 여러개의 공고를 벡터 변환 요청에 대한 응답
 */
@Data
public class NtceVectorResponse {
    private List<NtceVector> success;
    private List<NtceVectorFail> failed;

    /**
     * 벡터화 실패한 공고 정보
     */
    @Data
    public static class NtceVectorFail {
        private long bidNtceId;
        private String bidNtceNm;
        private String reason;
    }


}
