package com.naratil.batch.dto.nara.response;

import java.util.List;
import lombok.Data;

@Data
public class BidNoticeApiResponse {
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
        private List<BidNoticeItem> items;
    }

    @Data
    public static class BidNoticeItem {
        private String ntceKindNm;          // 공고 종류 (일반, 변경, 취소, 재입찰, 연기, 긴급, 갱신, 긴급갱신)
        private String bidNtceNo;           // 공고 번호
        private String bidNtceOrd;          // 공고 차수
        private String bidNtceNm;           // 공고명
        private String bsnsDivNm;           // 업무 구분 (물품, 용역, 공사)
        private String cntrctCnclsMthdNm;   // 계약 방법
        private String bidNtceDt;           // 공고 게시일
        private String bidBeginDt;          // 입찰 개시일
        private String bidClseDt;           // 입찰 마감일
        private String opengDt;             // 개찰일
        private String ntceInsttNm;         // 공고기관
        private String dminsttNm;           // 수요기관
        private String presmptPrce;         // 추정 가격
        private String prearngPrceDcsnMthdNm;   // 예가 방법
        private String sucsfbidLwltRate;    // 낙찰 하한율
        private String ntceInsttOfclNm;     // 담당자
        private String ntceInsttOfclTelNo;  // 연락처
        private String dcmtgOprtnDt;        // 설명회 일시
        private String dcmtgOprtnPlace;     // 설명회 장소
        private String bidNtceDtlUrl;       // 원문 URL
    }
}
