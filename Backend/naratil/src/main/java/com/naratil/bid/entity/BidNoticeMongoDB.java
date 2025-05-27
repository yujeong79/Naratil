package com.naratil.bid.entity;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 입찰 공고 정보를 표현하는 모델 클래스
 * 공공데이터 포털에서 제공하는 입찰공고 API 데이터를 저장하기 위한 MongoDB 문서 모델
 * 입찰 공고 번호와 차수를 기준으로 복합 인덱스가 설정되어 있어 중복 데이터를 방지
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "bids")
@CompoundIndex(name = "bids_compound_idx", def = "{'bidNtceNo': 1, 'bidNtceOrd': -1}", unique = true)
@CompoundIndex(name = "bids_vector_idx", def = "{'vector': 1, 'keywords': 1, 'bidNtceNm': 1}")
@CompoundIndex(name = "bids_indstrytyCd_idx", def = "{'indstrytyCd': 1}")
public class BidNoticeMongoDB {

    @Transient
    public static final String SEQUENCE_NAME = "bids_sequence";

    @Id
    private long bidNtceId;
    private String ntceKindNm;
    private String bidNtceNo;
    private String bidNtceOrd;
    private String bidNtceNm;
    private String bsnsDivNm;
    private String cntrctCnclsMthdNm;
    private LocalDateTime bidNtceDt;
    private LocalDateTime bidBeginDt;
    private LocalDateTime bidClseDt;
    private LocalDateTime opengDt;
    private String prtcptPsblRgnNm;
    private String indstrytyNm;
    private String indstrytyCd;
    private String ntceInsttNm;
    private String dminsttNm;
    private Long presmptPrce;
    private Long bssamt;
    private String prearngPrceDcsnMthdNm;
    private Double rsrvtnPrceRngBgnRate;
    private Double rsrvtnPrceRngEndRate;
    private Double sucsfbidLwltRate;
    private String ntceInsttOfclNm;
    private String ntceInsttOfclTelNo;
    private LocalDateTime dcmtgOprtnDt;
    private String dcmtgOprtnPlace;
    private String bidNtceDtlUrl;

//    private boolean isUpdated;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;


    // 배치용 정보
//    private List<Float> vector;
//    private List<String> keywords;

    private List<SimilarNtceItem> similarNtces;

    @Data
    @AllArgsConstructor
    public class SimilarNtceItem {
        private Long bidNtceId;
        private String bidNtceNm;
        private float finalScore;
    }

}
