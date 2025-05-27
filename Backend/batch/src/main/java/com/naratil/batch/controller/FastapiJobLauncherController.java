package com.naratil.batch.controller;

import com.naratil.batch.runner.FastApiJobRunner;
import com.naratil.batch.scheduler.FastApiScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


/**
 * fast api 관련 배치 job 수동 컨트롤러
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
@Tag(name = "유사 공고/추천 공고", description = "유사 공고/추천 선정 관련 API")
public class FastapiJobLauncherController {

    private final FastApiJobRunner fastApiJobRunner;

    @PostMapping("/current/index/create")
    @Operation(
            summary = "현재 공고 인덱싱 요청",
            description = "현재 공고에 대한 FAISS 인덱스 생성"
    )
    public ResponseEntity<String> runCreateCurrentIndexingJob() {
        log.info("[API 실행] 현재 공고 인덱싱 Job 실행 요청");
        fastApiJobRunner.runCreateCurrentIndexJob();
        return ResponseEntity.ok("현재 공고 인덱싱 실행 완료");
    }

    @PostMapping("/past/index/create")
    @Operation(
            summary = "과거 공고 인덱싱 요청",
            description = "과거 공고에 대한 FAISS 인덱스 생성"
    )
    public ResponseEntity<String> runCreatePastIndexingJob() {
        log.info("[API 실행] 과거 공고 인덱싱 Job 실행 요청");
        fastApiJobRunner.runCreatePastIndexJob();
        return ResponseEntity.ok("과거 공고 인덱싱 실행 완료");
    }

    @PostMapping("/past/index/update")
    @Operation(
            summary = "과거 공고 인덱스 업데이트 요청",
            description = "새로 추가된 과거 공고에 대해 키워드 추출 및 인덱스 업데이트 진행"
    )
    public ResponseEntity<String> runUpdatePastIndexingJob() {
        log.info("[API 실행] 과거 공고 인덱스 추가 Job 실행 요청");
        fastApiJobRunner.runUpdatePastIndexJob();
        return ResponseEntity.ok("과거 공고 인덱스 추가 실행 완료");
    }

    @PostMapping("/ntce/embedding")
    @Operation(
            summary = "현재 공고 임베딩 요청",
            description = "새로 추가된 현재 공고에 대해 임베딩(벡터화/키워드 추출) 작업 진행"
    )
    public ResponseEntity<String> runNtceEmbeddingJob() {
        log.info("[API 실행] 현재 공고 임베딩 배치 실행 요청");
        fastApiJobRunner.runNtceEmbeddingJob();
        return ResponseEntity.ok("현재 공고 임베딩 배치 실행 완료");
    }

    @PostMapping("/ntce/keywordextract")
    @Operation(
            summary = "과거 공고 키워드 추출 요청",
            description = "새로 추가된 과거 공고에 대해 키워드 추출 작업 진행"
    )
    public ResponseEntity<String> runNtceKeywordExtrationJob() {
        log.info("[API 실행] 공고 키워드 추출 배치 실행 요청");
        fastApiJobRunner.runKeywordExtractionJob();
        return ResponseEntity.ok("공고 키워드 추출 배치 실행 완료");
    }

    @PostMapping("/ntce/similarity")
    @Operation(
            summary = "유사 공고 선정 요청",
            description = "현재 공고에 대한 과거에 진행됐던 유사한 공고 선정"
    )
    public ResponseEntity<String> runNtceSimilarityJob() {
        log.info("[API 실행] 유사 공고 배치 실행 요청");
        fastApiJobRunner.runNtceSimilarityJob();
        return ResponseEntity.ok("유사 공고 배치 실행 완료");
    }

    @PostMapping("/ntce/similarity/empty")
    @Operation(
            summary = "유사 공고 추가 선정 요청",
            description = "같은 업종 코드로 산출이 되지 않은 경우 제한 없는 공고에서 유사한 공고 선정"
    )
    public ResponseEntity<String> runNtceSimilarityEmptyJob() {
        log.info("[API 실행] 유사 공고(Emtpy) 배치 실행 요청");
        fastApiJobRunner.runNtceSimilarityEmptyJob();
        return ResponseEntity.ok("유사 공고 배치(Emtpy) 실행 완료");
    }

    @PostMapping("/ntce/recommend")
    @Operation(
            summary = "추천 공고 선정 요청",
            description = "가입된 기업에 대한 현재 진행중인 맞춤 공고 선정"
    )
    public ResponseEntity<String> runRecommendJob() {
        log.info("[API 실행] 공고 추천 Job 실행 요청");
        fastApiJobRunner.runRecommendJob();
        return ResponseEntity.ok("공고 추천 Job 실행 완료");
    }

}

