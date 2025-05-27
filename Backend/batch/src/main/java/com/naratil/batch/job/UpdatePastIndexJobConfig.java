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
 * 과거 공고 FAISS 추가 Job Config
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class UpdatePastIndexJobConfig {

    private final JobRepository jobRepository;
    private final Step updatePastIndexStep;

    @Bean
    public Job updatePastIndexJob() {
//        log.info("[JOB_CONFIG] updatePastIndexJob 초기화 시작");
        return new JobBuilder("updatePastIndexJob", jobRepository)
                .start(updatePastIndexStep)  // 과거 공고 faiss 추가
                .build();
    }
}
