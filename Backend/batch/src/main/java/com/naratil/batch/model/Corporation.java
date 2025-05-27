package com.naratil.batch.model;

import lombok.Data;

/**
 * MySQL Corporation Table
 */
@Data
public class Corporation {
    private Long corpId; // 기업 순번
    private String bizno; // 사업자등록번호
    private int empNum; // 사원수
    private String industryCode; // 업종코드
}
