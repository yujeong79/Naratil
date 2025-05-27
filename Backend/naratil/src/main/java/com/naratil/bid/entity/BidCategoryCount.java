package com.naratil.bid.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Document(collection = "bids_cnt")
public class BidCategoryCount {
    @Field("major_category_code")
    private Long majorCategoryCode;

    @Field("count")
    private Integer count;
}
