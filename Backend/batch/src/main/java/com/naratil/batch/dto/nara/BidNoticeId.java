package com.naratil.batch.dto.nara;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
// 공고번호와 차수 정보를 담는 클래스
public class BidNoticeId {
    private String bidNtceNo;  // 공고번호
    private String bidNtceOrd; // 공고차수
    private String bsnsDivNm;   // 업무구분

    @Override
    public String toString() {
        return bidNtceNo + "-" + bidNtceOrd;
    }
}
