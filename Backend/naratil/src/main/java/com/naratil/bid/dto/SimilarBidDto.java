package com.naratil.bid.dto;

public record SimilarBidDto(
        Long bidNtceId,        // 공고 ID
        String bidNtceNm,      // 공고명
        Double finalScore       // 정규화된 유사도 점수

) {}
