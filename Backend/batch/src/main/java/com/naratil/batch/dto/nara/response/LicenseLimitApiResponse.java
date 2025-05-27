package com.naratil.batch.dto.nara.response;

import java.util.List;
import lombok.Data;

@Data
public class LicenseLimitApiResponse {
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
        private String pageNo;  // 페이지 번호
        private String numOfRows;   // 한 페이지 결과 수
        private String totalCount;  // 데이터 총 개수
        private List<LicenseLimitItem> items;
    }

    @Data
    public static class LicenseLimitItem {
        private String bidNtceNo;               // 입찰공고번호
        private String bidNtceOrd;              // 입찰공고차수
        private String lmtGrpNo;                // 제한그룹번호
        private String lmtSno;                  // 제한순번
        private String lcnsLmtNm;               // 면허제한명 (면허제한코드/면허제한명)
        private String permsnIndstrytyList;     // 허용업종목록
        private String rgstDt;                  // 등록일시
        private String bsnsDivNm;               // 업무구분명
        private String indstrytyMfrcFldList;    // 주력업종분야목록
    }
}
