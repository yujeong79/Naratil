package com.naratil.recommend.service;

import com.naratil.bid.entity.BidNoticeMongoDB;
import com.naratil.bid.repository.BidNoticeRepository;
import com.naratil.recommend.entity.Recommend;
import com.naratil.recommend.repository.RecommendRepository;
import com.naratil.security.jwt.service.JwtService;
import com.naratil.user.entity.User;
import com.naratil.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BidNoticeRepository bidNoticeRepository ;
    private final RecommendRepository recommendRepository;

    private final MongoTemplate mongoTemplate;

    public List<BidNoticeMongoDB> recommend(String authorization) {
        jwtService.validateAccessToken(authorization);// 통과하면 유효하다는 뜻
        String token = authorization.substring(7);

        Long userId = jwtService.getUserIdFromJwtToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

//        Corporation corporation = user.getCorporation();
//        if(corporation == null || corporation.getRecommends() == null) { // 등록된 기업이 없거나 기업에 추천공고가 없다면
//            return List.of(); // 빈리스트 반환
//        }
//        List<Recommend> recommends = corporation.getRecommends();   // 기업에 대한 추천공고 리스트
//        log.info("추천공고가 있음!!");
        List<Recommend> recommends = recommendRepository.findRecommendsByUserId(userId);
        List<Long> recoBidNums = new ArrayList<>(); // 추천공고번호들
        for(Recommend recommend : recommends) recoBidNums.add(recommend.getBidNtceId());    // 추천공고번호 주입

//        log.info("추천공고번호 리스트 : " + recoBidNums);
//        List<BidNoticeMongoDB> recoBids = bidNoticeRepository.findByBidNtceIdIn(recoBidNums);   // 공고번호로 공고가져옴
//        log.info("추천공고 리스트 : " + recoBids);
        
        return findRecommendDocs(recoBidNums);
    }

    /**
     * 추천 - 필요한 정보만
     * @param recommendIds
     * @return
     */
    private List<BidNoticeMongoDB> findRecommendDocs(List<Long> recommendIds) {
        Query query = Query.query(
                Criteria.where("_id").in(recommendIds)
        );

        query.fields()
                .include("_id")
                .include("bidNtceNm")
                .include("bidNtceDt")
                .include("bidClseDt")
                .include("dminsttNm")
                .include("presmptPrce")
        ;

        return mongoTemplate.find(query, BidNoticeMongoDB.class, "bids");
    }
}
