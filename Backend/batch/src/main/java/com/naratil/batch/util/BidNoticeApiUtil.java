package com.naratil.batch.util;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 나라장터 입찰공고정보서비스 API 호출을 위한 URI 생성 클래스
 */
@Slf4j
@Component
public class BidNoticeApiUtil {

    @Value("${public-data-api.service-key}")
    private String serviceKey;
    private String encodedServiceKey;
    private static final String END_POINT = "http://apis.data.go.kr/1230000/ad/BidPublicInfoService";

    /**
     * 빈 초기화시 service key 인코딩
     */
    @PostConstruct
    public void init() {
        this.encodedServiceKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);
        log.debug("🐛 BidInfoApiUtil Service Key 인코딩 성공");
    }

    public URI buildBidNoticeDateApiUri(String bsnsDivNm, int pageNo, int numOfRows, String inqryBgnDt, String inqryEndDt) {
        String apiName;

        switch (bsnsDivNm) {
            case "물품" -> apiName = "/getBidPblancListInfoThngPPSSrch";
            case "용역" -> apiName = "/getBidPblancListInfoServcPPSSrch";
            case "공사" -> apiName = "/getBidPblancListInfoCnstwkPPSSrch";
            default -> {
                log.error("🐛 입찰공고 조회 오류 : 물품/용역/공사만 조회 가능");
                apiName = "";
            }
        }

        return UriComponentsBuilder.fromUriString(END_POINT + apiName)
            .queryParam("serviceKey", encodedServiceKey)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("inqryDiv", "1")
            .queryParam("type", "json")
            .queryParam("inqryBgnDt", inqryBgnDt)
            .queryParam("inqryEndDt", inqryEndDt)
            .queryParam("bidClseExcpYn", "Y")
            .build(true)    // URI가 이미 인코딩 되어 있음
            .toUri();
    }

    public URI buildBidPriceDateApiUri(String bsnsDivNm, int pageNo, int numOfRows, String startDate, String endDate) {
        String apiName;

        switch (bsnsDivNm) {
            case "물품" -> apiName = "/getBidPblancListInfoThngBsisAmount";
            case "용역" -> apiName = "/getBidPblancListInfoServcBsisAmount";
            case "공사" -> apiName = "/getBidPblancListInfoCnstwkBsisAmount";
            default -> {
                log.error("🐛 입찰공고 기초금액 조회 오류 : 물품/용역/공사만 조회 가능");
                apiName = "";
            }
        }

        return UriComponentsBuilder.fromUriString(END_POINT + apiName)
            .queryParam("serviceKey", encodedServiceKey)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("inqryDiv", "1")
            .queryParam("type", "json")
            .queryParam("inqryBgnDt", startDate)
            .queryParam("inqryEndDt", endDate)
            .build(true)    // URI가 이미 인코딩 되어 있음
            .toUri();
    }

    public URI buildBidLicenseDateApiUri(int pageNo, int numOfRows, String inqryBgnDt, String inqryEndDt) {
        return UriComponentsBuilder.fromUriString(END_POINT + "/getBidPblancListInfoLicenseLimit")
            .queryParam("serviceKey", encodedServiceKey)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("inqryDiv", "1")
            .queryParam("type", "json")
            .queryParam("inqryBgnDt", inqryBgnDt)
            .queryParam("inqryEndDt", inqryEndDt)
            .build(true)    // URI가 이미 인코딩 되어 있음
            .toUri();
    }

    public URI buildBidRegionDateApiUri(int pageNo, int numOfRows, String inqryBgnDt, String inqryEndDt) {
        return UriComponentsBuilder.fromUriString(END_POINT + "/getBidPblancListInfoPrtcptPsblRgn")
            .queryParam("serviceKey", encodedServiceKey)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("inqryDiv", "1")
            .queryParam("type", "json")
            .queryParam("inqryBgnDt", inqryBgnDt)
            .queryParam("inqryEndDt", inqryEndDt)
            .build(true)    // URI가 이미 인코딩 되어 있음
            .toUri();
    }


    public URI buildBidNoticeNoApiUri(String bsnsDivNm, int pageNo, int numOfRows, String bidNtceNo) {
        String apiName;

        switch (bsnsDivNm) {
            case "물품" -> apiName = "/getBidPblancListInfoThng";
            case "용역" -> apiName = "/getBidPblancListInfoServc";
            case "공사" -> apiName = "/getBidPblancListInfoCnstwk";
            default -> {
                log.error("🐛 입찰공고 조회 오류 : 물품/용역/공사만 조회 가능");
                apiName = "";
            }
        }

        return UriComponentsBuilder.fromUriString(END_POINT + apiName)
            .queryParam("serviceKey", encodedServiceKey)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("inqryDiv", "2")
            .queryParam("type", "json")
            .queryParam("bidNtceNo", bidNtceNo)
            .queryParam("bidClseExcpYn", "Y")
            .build(true)    // URI가 이미 인코딩 되어 있음
            .toUri();
    }

    public URI buildBidPriceNoApiUri(String bsnsDivNm, int pageNo, int numOfRows, String bidNtceNo, String bidNtceOrd) {
        String apiName;

        switch (bsnsDivNm) {
            case "물품" -> apiName = "/getBidPblancListInfoThngBsisAmount";
            case "용역" -> apiName = "/getBidPblancListInfoServcBsisAmount";
            case "공사" -> apiName = "/getBidPblancListInfoCnstwkBsisAmount";
            default -> {
                log.error("🐛 입찰공고 기초금액 조회 오류 : 물품/용역/공사만 조회 가능");
                apiName = "";
            }
        }

        return UriComponentsBuilder.fromUriString(END_POINT + apiName)
            .queryParam("serviceKey", encodedServiceKey)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("inqryDiv", "2")
            .queryParam("type", "json")
            .queryParam("bidNtceNo", bidNtceNo)
            .queryParam("bidNtceOrd", bidNtceOrd)
            .build(true)    // URI가 이미 인코딩 되어 있음
            .toUri();
    }

    public URI buildBidLicenseNoApiUri(int pageNo, int numOfRows, String bidNtceNo, String bidNtceOrd) {
        return UriComponentsBuilder.fromUriString(END_POINT + "/getBidPblancListInfoLicenseLimit")
            .queryParam("serviceKey", encodedServiceKey)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("inqryDiv", "2")
            .queryParam("type", "json")
            .queryParam("bidNtceNo", bidNtceNo)
            .queryParam("bidNtceOrd", bidNtceOrd)
            .build(true)    // URI가 이미 인코딩 되어 있음
            .toUri();
    }

    public URI buildBidRegionNoApiUri(int pageNo, int numOfRows, String bidNtceNo, String bidNtceOrd) {
        return UriComponentsBuilder.fromUriString(END_POINT + "/getBidPblancListInfoPrtcptPsblRgn")
            .queryParam("serviceKey", encodedServiceKey)
            .queryParam("pageNo", pageNo)
            .queryParam("numOfRows", numOfRows)
            .queryParam("inqryDiv", "2")
            .queryParam("type", "json")
            .queryParam("bidNtceNo", bidNtceNo)
            .queryParam("bidNtceOrd", bidNtceOrd)
            .build(true)    // URI가 이미 인코딩 되어 있음
            .toUri();
    }
}
