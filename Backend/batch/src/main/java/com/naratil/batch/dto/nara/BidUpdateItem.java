package com.naratil.batch.dto.nara;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BidUpdateItem {
    private String bidNtceNo;   // 입찰공고번호
    private String bidNtceOrd;  // 입찰공고차수
    private String bssamt;  // 기초금액
    private String rsrvtnPrceRngBgnRate; // 예가 변동폭 시작률
    private String rsrvtnPrceRngEndRate; // 예가 변동폭 종료률
    private String lcnsLmtNm;   // 면허제한명 (업종코드/업종명)
    private String indstrytyCd; // 업종코드
    private String indstrytyNm; // 업종명
    private String prtcptPsblRgnNm; // 참가가능지역명
}
