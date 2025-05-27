package com.naratil.batch.processor;

import com.naratil.batch.dto.fastapi.common.NtceBase;
import com.naratil.batch.dto.fastapi.embedding.notice.NtceVector;
import com.naratil.batch.dto.fastapi.embedding.notice.NtceVectorResponse;
import com.naratil.batch.model.BidNotice;
import com.naratil.batch.service.NtceEmbeddingService;
import com.naratil.batch.util.KeywordExtractor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 공고 벡터화 및 키워드 추출을 처리하는 Processor
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NtceVectorProcessor implements ItemProcessor<List<NtceBase>, List<BidNotice>> {

    private final NtceEmbeddingService embeddingService;
    private final KeywordExtractor keywordExtractor;

    @Override
    public List<BidNotice> process(List<NtceBase> ntceBases) throws Exception {
        log.debug("[Processor 시작] 입력: {}건", ntceBases.size());

        // FastAPI 호출
        NtceVectorResponse response = embeddingService.embed(ntceBases);

        List<BidNotice> resultList = new ArrayList<>();

        // 성공한 벡터화 결과 기준으로 BidNotice 생성
        for (NtceVector vectorItem : response.getSuccess()) {
            List<String> keywords = keywordExtractor.extractCleanKeywords(vectorItem.getBidNtceNm());
            log.debug("공고 ID: {}, 공고명: {},  추출 키워드: {}", vectorItem.getBidNtceId(), vectorItem.getBidNtceNm(), keywords);
            BidNotice notice = BidNotice.builder()
                    .bidNtceId(vectorItem.getBidNtceId())
                    .vector(vectorItem.getVector())
                    .keywords(keywords)
                    .build();

            resultList.add(notice);
        }

        log.info("[Processor] 현재 공고 임베딩 완료 - {}건", resultList.size());

        return resultList;
    }
}
