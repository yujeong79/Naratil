package com.naratil.bid.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.bson.Document;


@Getter
@Builder
public class BidDetailResponseDto {
    private Document currentBids;
    private List<Document> pastData;
}
