package com.naratil.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Recommend {
    private long corpId;
    private long bidNtceId;
    private float score;
}
