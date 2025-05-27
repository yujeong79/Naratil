package com.naratil.batch.dto.nara.response;

import java.util.List;
import lombok.Data;

@Data
public class BasicPriceApiResponse {
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
        private List<BasicPriceItem> items;
    }

    @Data
    public static class BasicPriceItem {
        private String bidNtceNo;            // 입찰공고번호
        private String bidNtceOrd;           // 입찰공고차수
        private String bssamt;               // 기초금액
        private String rsrvtnPrceRngBgnRate; // 예가 변동폭 시작률
        private String rsrvtnPrceRngEndRate; // 예가 변동폭 종료률
    }
}
