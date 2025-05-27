package com.naratil.batch.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

/**
 * 입찰공고 배치 작업을 실행하기 위한 클래스
 * 컨트롤러나 스케쥴러 등에서 호출하여 배치 작업을 실행
 * JobLauncher를 통해 Job을 실행하고, JobParameters를 구성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BidNoticeJobRunner {

    // Job 실행을 담당하는 컴포넌트
    private final JobLauncher jobLauncher;

    // 입찰공고 수집 배치 작업
    private final Job bidNoticeJob;
    // 중복제거, 면허제한, 지역제한 정보 업데이트 작업
    private final Job bidUpdateJob;
    // 입찰공고 컬렉션 초기화
    private final Job bidInitJob;

    /**
     * 날짜 범위를 지정하여 입찰 공고 데이터 수집
     *
     * @param startDate 시작 날짜 (YYYYMMDDHHMM)
     * @param endDate 종료 날짜 (YYYYMMDDHHMM)
     */
    public void runBidNoticeJobWithDateRange(String startDate, String endDate) {
        log.debug("🐛 입찰 공고 수집 시작 : {} - {}", startDate, endDate);
        // TODO 입력된 날짜 형식 유효성 검증 필요
        
        // 배치 작업에 전달할 JobParameters 구성
        JobParameters jobParameters = new JobParametersBuilder()
            .addString("startDate", startDate)
            .addString("endDate", endDate)
            .addLong("time", System.currentTimeMillis())    // 작업 식별을 위한 타임스탬프
            .toJobParameters();

        try {
            // Job 실행
            JobExecution jobExecution = jobLauncher.run(bidNoticeJob, jobParameters);
            log.info("🐛 입찰 공고 수집 완료 : {} - {}, 시작: {}, 종료: {}",  startDate, endDate, jobExecution.getStartTime(), jobExecution.getEndTime());
        } catch (Exception e) {
            log.error("🐛 입찰 공고 수집 실패 : {}", e.getMessage());
        }
    }

    public void runBidUpdateJob() {
        log.debug("🐛 입찰 공고 수정 시작");

        // 배치 작업에 전달할 JobParameters 구성
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())    // 작업 식별을 위한 타임스탬프
            .toJobParameters();

        try {
            // Job 실행
            JobExecution jobExecution = jobLauncher.run(bidUpdateJob, jobParameters);
            log.info("🐛 입찰 공고 수정 완료, 시작: {}, 종료: {}", jobExecution.getStartTime(), jobExecution.getEndTime());
        } catch (Exception e) {
            log.error("🐛 입찰 공고 수정 실패 : {}", e.getMessage());
        }
    }

    public void runBidInitJob() {
        log.info("🐛 입찰공고 초기화");

        // 배치 작업에 전달할 JobParameters 구성
        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("time", System.currentTimeMillis())    // 작업 식별을 위한 타임스탬프
            .toJobParameters();

        try {
            // Job 실행
            JobExecution jobExecution = jobLauncher.run(bidInitJob, jobParameters);
            log.info("🐛 입찰공고 초기화 완료, 시작: {}, 종료: {}", jobExecution.getStartTime(), jobExecution.getEndTime());
        } catch (Exception e) {
            log.error("🐛 입찰공고 초기화 실패 : {}", e.getMessage());
        }
    }

}
