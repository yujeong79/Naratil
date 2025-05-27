package com.naratil.batch.dto.fastapi.similarity;

import lombok.Data;

import java.util.List;

@Data
public class BidNtce {
    private Long bidNtceId;
    private String bidNtceNm;
    private List<Float> vector;
    private List<String> keywords;
    private String indstrytyCd;

}
