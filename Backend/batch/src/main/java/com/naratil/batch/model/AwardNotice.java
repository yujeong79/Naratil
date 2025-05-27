package com.naratil.batch.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 낙찰 공고 (입찰 마감 공고) - 컬렉션 : industry_*
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name = "bids_vector_idx", def = "{'vector': 1, 'keywords': 1, 'bidNtceNm': 1}")
@CompoundIndex(name = "bids_similarity_idx", def = "{'vector': 1, 'keywords': 1, 'bidNtceNm': 1, 'bidNtceDt': -1}")
@CompoundIndex(name = "bids_indstrytyCd_idx",def = "{'indstrytyCd': 1}")
public class AwardNotice {

    private int bidNtceId;          // 공고 ID
    private String bidNtceNo;          // 공고 번호
    private LocalDateTime bidBeginDt; // 입찰 시작 일자
    private LocalDateTime bidClseDt; // 입찰 마감 일자
    private String bidNtceNm;          // 공고명
    private String indstrytyCd;        // 업종코드
    private List<Float> vector; // 공고명 벡터
    private List<String> keywords; // 공고명 키워드
    private String awardCorp; // 낙찰 기업 사업자번호
    private List<String> candidates; // 입찰 참여기업 사업자 번호
}
