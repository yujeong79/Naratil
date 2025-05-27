package com.naratil.batch.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * Fast Api 관련 Job Runner
 * - 과거 공고 키워드 추출
 * - 현재 공고 임베딩
 * - 유사 공고 선정(업종 제한 없음)
 * - 유사 공고 선정
 * - 추천 공고 선정
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FastApiJobRunner {

    private final JobLauncher jobLauncher;

    private final Job keywordExtractionJob;
    private final Job ntceEmbeddingJob;
    private final Job ntceSimilarityJob;
    private final Job ntceSimilarityEmptyJob;
    private final Job recommendJob;
    private final Job createPastIndexJob;
    private final Job updatePastIndexJob;
    private final Job createCurrentIndexJob;

    private JobParameters createJobParameters() {
        return new JobParametersBuilder()
                .addString("runTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .toJobParameters();
    }

    private void runJob(Job job, String jobName) {
        try {
            log.info("[Runner] {} Job 실행 시작: {}", jobName, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            Instant start = Instant.now();
            JobExecution execution = jobLauncher.run(job, createJobParameters());
            Duration duration = Duration.between(start, Instant.now());
            log.info("[Runner] {} Job 완료 상태: {}, 소요: {}분({}초)", jobName, execution.getStatus(), duration.toMinutes(), duration.toSeconds());
            if (execution.getStatus() != BatchStatus.COMPLETED) {
                log.warn("[Runner] {} Job 일부 Step 실패 또는 중단: {}", jobName, execution.getExitStatus());
            }
        } catch (Exception e) {
            log.error("[Runner] {} Job 실행 실패: {}", jobName, e.getMessage(), e);
        }
    }

    public void runKeywordExtractionJob() {
        runJob(keywordExtractionJob, "키워드 추출");
    }

    public void runNtceEmbeddingJob() {
        runJob(ntceEmbeddingJob, "공고 임베딩");
    }

    public void runNtceSimilarityJob() {
        runJob(ntceSimilarityJob, "유사 공고 선정");
    }

    public void runNtceSimilarityEmptyJob() {
        runJob(ntceSimilarityEmptyJob, "유사 공고 추가 선정(Empty -> no_limit");
    }

    public void runCreatePastIndexJob() {
        runJob(createPastIndexJob, "과거 공고 인덱스 초기화/생성");
    }

    public void runUpdatePastIndexJob() {
        runJob(updatePastIndexJob, "과거 공고 인덱스 업데이트");
    }

    public void runCreateCurrentIndexJob() {
        runJob(createCurrentIndexJob, "현재 공고 인덱스 초기화/생성");
    }

    public void runRecommendJob() {
        runJob(recommendJob, "기업별 공고 추천");
    }

}
