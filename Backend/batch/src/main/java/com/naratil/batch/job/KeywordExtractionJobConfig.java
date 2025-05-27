package com.naratil.batch.job;

import com.naratil.batch.tasklet.NtceKeywordExtractionTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 과거 공고 키워드 추출 배치 Job
 * - 과거 공고 컬렉션마다 순회하면서 keywords가 없는 문서들을 뽑아 키워드 추출 후 update
 */
@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class KeywordExtractionJobConfig {

    private final NtceKeywordExtractionTasklet industryKeywordExtractionTasklet;

    @Bean
    public Job keywordExtractionJob(
            JobRepository jobRepository,
            Step industryKeywordExtractionStep
    ) {
//        log.info("[JOB_CONFIG] keywordExtractionJob 초기화");
        return new JobBuilder("keywordExtractionJob", jobRepository)
                .start(industryKeywordExtractionStep)
                .build();
    }

    @Bean
    public Step industryKeywordExtractionStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
//        log.info("[JOB_CONFIG] industryKeywordExtractionStep 초기화");
        return new StepBuilder("industryKeywordExtractionStep", jobRepository)
                .tasklet(industryKeywordExtractionTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
