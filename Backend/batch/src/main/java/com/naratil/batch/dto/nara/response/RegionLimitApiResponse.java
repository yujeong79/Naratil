package com.naratil.batch.dto.nara.response;

import java.util.List;
import lombok.Data;

@Data
public class RegionLimitApiResponse {

    private Response response;

    @Data
    public static class Response {
        private Header header;
        private Body body;
    }

    @Data
    public static class Header {
        private String resultCode;  // 결과코드
        private String resultMsg;   // 결과메세지
    }

    @Data
    public static class Body {
        private String pageNo;      // 페이지 번호
        private String numOfRows;   // 한 페이지 결과 수
        private String totalCount;  // 데이터 총 개수
        private List<RegionLimitItem> items;
    }

    @Data
    public static class RegionLimitItem {
        private String bidNtceNo;       // 입찰공고번호
        private String bidNtceOrd;      // 입찰공고차수
        private String lmtSno;          // 제한순번
        private String prtcptPsblRgnNm; // 참가가능지역명
        private String rgstDt;          // 등록일시
        private String bsnsDivNm;       // 업무구분명
    }
}
