package com.naratil.batch.dto.fastapi.indexing;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * FAISS 인덱스에 추가할 요청 정보
 */
@Data
public class AddIndexRequest {
    private String collectionName; // 컬렉션 이름
    private Map<Long, List<String>> keywordsMap; // 키워드 리스트
    private List<List<Float>> vectors; // 벡터 리스트
    private List<Long> ids; // ID 리스트
}
