package com.naratil.bid.repository;

import com.naratil.bid.entity.BidNoticeMongoDB;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BidNoticeRepository extends MongoRepository<BidNoticeMongoDB, Long> {

    List<BidNoticeMongoDB> findByBidNtceIdIn(List<Long> bidNtceIds); // 공고번호 리스트로 공고상세데이터 리스트 조회

}
