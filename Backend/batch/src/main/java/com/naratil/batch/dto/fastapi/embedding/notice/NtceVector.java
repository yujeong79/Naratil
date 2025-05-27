package com.naratil.batch.dto.fastapi.embedding.notice;

import lombok.Data;

import java.util.List;

/**
 * 공고 벡터
 */
@Data
public class NtceVector {
    private long bidNtceId; // 공고 ID
    private String bidNtceNm; // 공고명
    private List<Float> vector; // 공고명 벡터
}
