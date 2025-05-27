package com.naratil.batch.dto.fastapi.similarity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarNtcesResponse {
    private Map<Long, List<SimilarNtceItem>> results;
}
