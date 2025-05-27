package com.naratil.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 과거 공고 FAISS 인덱싱용 Job Config
 * - Clear -> Create 순서로 Step 실행
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CreatePastIndexJobConfig {

    private final JobRepository jobRepository;
    private final Step clearPastIndexStep;
    private final Step createPastIndexStep;

    @Bean
    public Job createPastIndexJob() {
//        log.info("[JOB_CONFIG] CreatePastIndexJob 초기화 시작");

        return new JobBuilder("createPastIndexJob", jobRepository)
                .start(clearPastIndexStep)  // 과거 인덱스 초기화
                .next(createPastIndexStep)  // 과거 인덱스 생성
                .build();
    }
}
