package com.naratil.corporation.service;

import com.naratil.corporation.entity.Corporation;
import com.naratil.corporation.repository.CorpRepository;
import com.naratil.corporation.dto.CorpRequestDto;
import com.naratil.industry.entity.Industry;
import com.naratil.industry.repository.IndustryRepository;
import com.naratil.recommend.service.RecommendCreator;
import com.naratil.security.jwt.service.JwtService;
import com.naratil.user.entity.User;
import com.naratil.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CorpService {

    private final CorpRepository corpRepository;
    private final UserRepository userRepository;
    private final IndustryRepository industryRepository;
    private final JwtService jwtService;

    private final RecommendCreator recommendCreator;

    public void signup(CorpRequestDto corpRequestDto, String token) {

        //토큰에서 userId가져오기
        Long userId = jwtService.getUserIdFromJwtToken(token);

        // 업종코드로 업종객체 가져오기
        Industry industry = industryRepository.findByIndustryCode(corpRequestDto.IndustryCode())
                        .orElseThrow(() -> new RuntimeException("Industry Not Found"));
        log.info("industry : " + industry);

        // 기업 정보를 DB에서 조회 (존재하는 기업인지 확인)
        Corporation corporation = corpRepository.findByBizno(corpRequestDto.businessNumber())
                .orElse(null);

        boolean newCorp = false;
        if (corporation != null) {// 기존 기업이 존재하면 값만 업데이트
            corporation.update(corpRequestDto, industry);
            log.info("기존 기업 정보 업데이트 완료");
        } else {// 기업이 없으면 새로 생성
            corporation = new Corporation(corpRequestDto, industry);
            log.info("신규 기업 정보 생성 완료");
            newCorp = true;
        }
        Corporation corp = corpRepository.save(corporation);

        if(newCorp) {
            recommendCreator.createRecommend(corp);
        }

        log.info("기업 정보 저장 완료");

        // 기존 user를 조회
        User existingUser = userRepository.findById(userId).orElse(null);

        if (existingUser != null) {
            User user = User.builder()
                    .id(existingUser.getId())
                    .name(existingUser.getName())
                    .email(existingUser.getEmail())
                    .password(existingUser.getPassword())
                    .corporation(corp)    // 기업 맵핑
                    .build();
            // 새로운 user 저장
            userRepository.save(user);
            log.info("새로운 user 저장 완료");
        }

    }

}
