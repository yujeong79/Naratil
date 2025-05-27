package com.naratil.batch.dto.fastapi.embedding.notice;

import com.naratil.batch.dto.fastapi.common.NtceBase;
import lombok.Data;

import java.util.List;

/**
 * 1개 이상의 공고를 벡터 변환 요청
 */
@Data
public class NtceVectorRequest {
    private List<NtceBase> ntces; // 벡터로 변환할 공고 정보 리스트
}
