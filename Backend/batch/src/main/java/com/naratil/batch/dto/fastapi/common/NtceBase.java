package com.naratil.batch.dto.fastapi.common;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공고 기본 정보(공고 ID, 공고명)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NtceBase {
    private long bidNtceId; // 공고 ID
    private String bidNtceNm; // 공고명
}
