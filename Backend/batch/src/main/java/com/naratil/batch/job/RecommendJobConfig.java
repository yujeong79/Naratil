package com.naratil.batch.job;

import com.naratil.batch.listener.RecommendJobListener;
import com.naratil.batch.model.Corporation;
import com.naratil.batch.model.Recommend;
import com.naratil.batch.processor.RecommendProcessor;
import com.naratil.batch.reader.RecommendReader;
import com.naratil.batch.writer.RecommendWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * 추천 공고 배치 Job 설정
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RecommendJobConfig {

    private final RecommendReader reader;
    private final RecommendProcessor processor;
    private final RecommendWriter writer;
    private final RecommendJobListener listener;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private static final int CHUNK_SIZE = 5; // 5 청크

    @Bean
    public Job recommendJob() {
        return new JobBuilder("recommendJob", jobRepository)
                .start(recommendStep())
                .listener(listener)
                .build();
    }

    @Bean
    public Step recommendStep() {
        return new StepBuilder("recommendStep", jobRepository)
                .<List<Corporation>, List<Recommend>>chunk(CHUNK_SIZE, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }
}
