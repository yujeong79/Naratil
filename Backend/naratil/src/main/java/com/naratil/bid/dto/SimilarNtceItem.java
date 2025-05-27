package com.naratil.bid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SimilarNtceItem {
    private int bidNtceId;
    private String bidNtceNm;
    private float finalScore;
}