package com.naratil.batch.controller;

import com.naratil.batch.scheduler.BidNoticeScheduler;
import com.naratil.batch.scheduler.FastApiScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 스케쥴링으로 돌아가는 배치 job 수동 컨트롤러 (스케줄 단위(O), 작업 단위(X))
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
@Tag(name = "스케줄 단위 공고 호출", description = "진행중인 공고 수집/유사 공고 선정/공고 추천")
public class ScheduledJobController {

    private final BidNoticeScheduler bidNoticeScheduler;
    private final FastApiScheduler fastApiScheduler;

    @PostMapping("/job/execute/daily")
    @Operation(
            summary = "매일 자정(1:30)마다 실행되는 작업 즉시 진행 요청",
            description = "과거 공고 키워드 추출 및 인덱스 업데이트 -> 현재 공고 임베딩 -> 현재 공고에 대한 과거 유사 공고 선정 -> 현재 공고 인덱스 초기화 및 생성 -> 기업 맞춤 공고 선정 "
    )
    public ResponseEntity<String> runDailyJob() {
        log.info("[API 실행] 일일 FastAPI Job 실행 요청");
        fastApiScheduler.scheduleFastApiJobDaily();
        return ResponseEntity.ok("자정 FastAPI Job 실행 완료");
    }


    @PostMapping("/job/execute/monthly")
    @Operation(
            summary = "매월 1일(23:00)마다 실행되는 작업 즉시 진행 요청",
            description = "과거 공고 인덱스 초기화 및 생성/업데이트"
    )
    public ResponseEntity<String> runMonthlyJob() {
        log.info("[API 실행] 월간 FastAPI Job 실행 요청");
        fastApiScheduler.scheduleFastApiJobMonthly();
        return ResponseEntity.ok("월간 FastAPI Job 실행 완료");
    }


    @PostMapping("/job/execute/multitime")
    @Operation(
            summary = "매일 여러번 실행되는 작업 즉시 진행 요청",
            description = "새로 추가된 현재 공고에 대한 임베딩 및 과거 유사 공고 선정"
    )
    public ResponseEntity<String> runMultiTimeJob() {
        log.info("[API 실행] 반복 FastAPI Job 실행 요청");
        fastApiScheduler.scheduleFastApiJobMultiTime();
        return ResponseEntity.ok("반복 FastAPI Job 실행 완료");
    }

    @PostMapping("/job/execute/daily/fetch")
    @Operation(
            summary = "매일 자정 실행되는 현재 공고 수집 작업 진행 요청",
            description = "컬렉션 초기화 및 현재 공고 수집"
    )
    public ResponseEntity<String> runDailyFetchJob() {
        log.info("[API 실행] 일일 Fetch Job 실행 요청");
        bidNoticeScheduler.scheduleBidNoticeFetch();
        return ResponseEntity.ok(" 일일 Fetch Job 실행 완료");
    }

    @PostMapping("/job/execute/multitime/fetch")
    @Operation(
            summary = "매일 여러번 실행되는 새로 등록된 공고 수집 작업 진행 요청",
            description = "새로 등록된 공고 수집 및 업데이트"
    )
    public ResponseEntity<String> runMultiTimeFetchJob() {
        log.info("[API 실행] 반복 Fetch Job 실행 요청");
        bidNoticeScheduler.scheduleBidNoticeFetch4PM();
        return ResponseEntity.ok(" 반복 Fetch Job 실행 완료");
    }
}
