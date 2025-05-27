package com.naratil.batch.scheduler;

import com.naratil.batch.runner.FastApiJobRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JOB 실행 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiScheduler {

    // 선행 작업 이름 : 초기화 -> 수집 -> 업데이트
    private static final String[] BEFORE_JOB_NAMES = {"bidInitJob", "bidNoticeJob", "bidUpdateJob"};

    private final FastApiJobRunner fastApiJobRunner;
    private final JobExplorer jobExplorer;

    /**
     * 스케줄링 매일 1시 30분 (자정 수집이 완료되는 것을 기다렸다가 진행)
     * - 과거 공고 인덱스 업데이트 및 키워드 추출
     * - 현재 공고 임베딩 - 공통
     * - 유사 공고 선정 - 공통
     * - 현재 공고 인덱싱 - (FOR 추천)
     * - 추천 공고 선정
     *
     * - (-9시간)으로 설정하여 서버시간 자정에 맞춤
     */
//    @Scheduled(cron = "0 30 16 * * ?")
    public void scheduleFastApiJobDaily() {
        log.info("[Scheduler] 일일(1:30) fastapi 스케줄링 시작: {}", LocalDateTime.now());
        Instant start = Instant.now();
        try {
            waitForPreviousThenRun(BEFORE_JOB_NAMES);
            fastApiJobRunner.runUpdatePastIndexJob(); // 과거 공고 추가 인덱싱 및 키워드 추출
            runFastApiCommonJob(); // 현재 공고 임베딩 + 유사 공고
            fastApiJobRunner.runCreateCurrentIndexJob(); // 쳔재 공고 인덱싱 for 추천
            fastApiJobRunner.runRecommendJob(); // 추천

        } catch (Exception e) {
            log.error("[Scheduler] fastapi 자정 Job 실행 중 오류 발생", e);
        }

        Duration duration = Duration.between(start, Instant.now());
        log.info("[Scheduler] 일일(1:30) fastapi 스케줄링 종료 {}, 소요: {}분({}초)", LocalDateTime.now(), duration.toMinutes(), duration.toSeconds());
    }


    /**
     * 스케줄링 매일 8시, 11시, 14시, 16시 - 5분
     * - 현재 공고 임베딩
     * - 유사 공고 선정
     *
     * - 서버시간 (-9)시
     */
//    @Scheduled(cron = "0 5 2,5,7,23 * * ?")
    public void scheduleFastApiJobMultiTime() {
        log.info("[Scheduler] fastapi 반복 작업 스케줄링 시작: {}", LocalDateTime.now());
        Instant start = Instant.now();
        waitForPreviousThenRun(BEFORE_JOB_NAMES);
        runFastApiCommonJob();
        Duration duration = Duration.between(start, Instant.now());
        log.info("[Scheduler] fastapi 반복 스케줄링 종료 {}, 소요: {}분({}초)", LocalDateTime.now(), duration.toMinutes(), duration.toSeconds());
    }

    /**
     * 매월 1일 23시 과거 인덱스 초기화
     *
     * - 서버시간 14시
     */
//    @Scheduled(cron = "0 0 14 1 * ?")
    public void scheduleFastApiJobMonthly() {
        log.info("[Scheduler] fastapi 월간 작업 스케줄링 시작: {}", LocalDateTime.now());
        Instant start = Instant.now();
        fastApiJobRunner.runCreatePastIndexJob();
        Duration duration = Duration.between(start, Instant.now());
        log.info("[Scheduler] fastapi 월간 작업 스케줄링 종료 {}, 소요: {}분({}초)", LocalDateTime.now(), duration.toMinutes(), duration.toSeconds());
    }


    /**
     * 자정, 낮 공통 작업
     * - 현재 공고 임베딩
     * - 현재 공고 - 유사 공고 선정
     */
    private void runFastApiCommonJob() {
        fastApiJobRunner.runNtceEmbeddingJob(); // 현재 공고 임베딩
        fastApiJobRunner.runNtceSimilarityJob(); // 유사 공고
        fastApiJobRunner.runNtceSimilarityEmptyJob();
    }

    /**
     * 앞선 Job이 끝날 때까지 대기
     * @param jobNames :선행 작업 이름들
     */
    private void waitForPreviousThenRun(String[] jobNames) {
        int waited = 0;
        while (areAnyJobsRunning(jobNames)) {
            log.warn("[Scheduler] 이전 작업 중 - {}분 대기 중", waited);
            try {
                Thread.sleep(300_000); // 5분 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            waited = waited + 5;
        }
    }

    /**
     * 선행 작업이 하나라도 실행 중인지
     * @param jobNames : 작업 이름
     * @return 선행 작업 실행중 여부
     */
    private boolean areAnyJobsRunning(String[] jobNames) {
        for (String jobName : jobNames) {
            List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, 1);
            if (!instances.isEmpty()) {
                List<JobExecution> executions = jobExplorer.getJobExecutions(instances.get(0));
                if (!executions.isEmpty() && executions.get(0).isRunning()) {
                    log.warn("[Scheduler] Job '{}' 실행 중", jobName);
                    return true;
                }
            }
        }
        return false;
    }

}