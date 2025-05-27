package com.naratil.batch.job;

import com.naratil.batch.reader.CreateCurrentIndexReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * 현재 공고 FAISS 인덱싱용 Job Config
 * - Clear -> Create 순서로 Step 실행
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CreateCurrentIndexJobConfig {

    private final MongoTemplate mongoTemplate;
    private final JobRepository jobRepository;

    private final Step clearCurrentIndexStep;
    private final Step createCurrentIndexStep;

    @Bean
    public Job createCurrentIndexJob() {
//        log.info("[JOB_CONFIG] createCurrentIndexJob 초기화 시작");
        return new JobBuilder("createCurrentIndexJob", jobRepository)
                .start(clearCurrentIndexStep)  // 현재 인덱스 초기화
                .next(createCurrentIndexStep)  // 현재 인덱스 생성
                .build();
    }

    @Bean
    @StepScope
    public CreateCurrentIndexReader createCurrentIndexReader() {
        return new CreateCurrentIndexReader(mongoTemplate);
    }
}
