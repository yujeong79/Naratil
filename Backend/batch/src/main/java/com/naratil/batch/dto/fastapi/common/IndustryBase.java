package com.naratil.batch.dto.fastapi.common;

import lombok.Data;

/**
 * 벡터화 하기 위해 필요한 업종 정보(업종 ID, 업종명)
 */
@Data
public class IndustryBase {
    private int industryId; // 업종 ID
    private String industryNm; // 업종명
}
