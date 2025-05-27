package com.naratil.batch.model;

import java.time.LocalDateTime;
import java.util.List;

import com.naratil.batch.dto.fastapi.similarity.SimilarNtceItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
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
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Document(collection = "bids")
@CompoundIndex(name = "bids_compound_idx", def = "{'bidNtceNo': 1, 'bidNtceOrd': -1}", unique = true)
@CompoundIndex(name = "bids_vector_idx", def = "{'vector': 1, 'keywords': 1, 'bidNtceNm': 1}")
@CompoundIndex(name = "bids_indstrytyCd_idx",def = "{'indstrytyCd': 1}")

public class BidNotice {

    @Transient  // Java 객체에만 존재하고 MongoDB 문서에는 저장되지 않음
    public static final String SEQUENCE_NAME = "bids_sequence";

    @Id
    private long bidNtceId;          // 공고 시퀀스
    private String ntceKindNm;         // 공고 종류 (일반, 변경, 취소, 재입찰, 연기, 긴급, 갱신, 긴급갱신)
    private String bidNtceNo;          // 공고 번호
    private String bidNtceOrd;         // 공고 차수
    private String bidNtceNm;          // 공고명
    private String bsnsDivNm;          // 업무 구분 (물품, 용역, 공사)
    private String cntrctCnclsMthdNm;  // 계약 방법
    private LocalDateTime bidNtceDt;   // 공고 게시일
    private LocalDateTime bidBeginDt;  // 입찰 개시일
    private LocalDateTime bidClseDt;  // 입찰 마감일
    private LocalDateTime opengDt;     // 개찰일
    private String prtcptPsblRgnNm;    // 참가제한지역
    private String indstrytyNm;        // 업종명
    private String indstrytyCd;        // 업종코드
    private String ntceInsttNm;        // 공고기관
    private String dminsttNm;          // 수요기관
    private Long presmptPrce;          // 추정 가격
    private Long bssamt;              // 기초 금액
    private String prearngPrceDcsnMthdNm; // 예가 방법
    private Double rsrvtnPrceRngBgnRate;  // 예가 변동폭 시작
    private Double rsrvtnPrceRngEndRate; // 예가 변동폭 끝
    private Double sucsfbidLwltRate;   // 낙찰 하한율
    private String ntceInsttOfclNm;    // 담당자
    private String ntceInsttOfclTelNo; // 연락처
    private LocalDateTime dcmtgOprtnDt; // 설명회 일시
    private String dcmtgOprtnPlace;    // 설명회 장소
    private String bidNtceDtlUrl;      // 원문 URL

    private boolean isUpdated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Float> vector; // 공고명 벡터
    private List<String> keywords; // 공고명 키워드
    private List<SimilarNtceItem> similarNtces; // 과거 유사 공고 정보
}
