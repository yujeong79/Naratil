package com.naratil.batch.job;

import com.naratil.batch.client.fastapi.SimilarityApiClient;
import com.naratil.batch.common.MongoCollectionChecker;
import com.naratil.batch.dto.fastapi.similarity.BidNtce;
import com.naratil.batch.writer.SimilarNtcesEmptyWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * 유사 공고 배치 Job 구성 클래스
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class NtceSimilarityEmptyJobConfig {

    private final MongoTemplate mongoTemplate;
    private final MongoCollectionChecker collectionChecker;
    private final SimilarityApiClient apiClient;
    private final ItemReader<List<BidNtce>> targetReader;

    private final static int CHUNK_SIZE = 10;

    @Bean
    public Job ntceSimilarityEmptyJob(JobRepository jobRepository, Step ntceSimilarityEmptyStep) {
//        log.info("[JOB_CONFIG] ntceSimilarityJob 초기화 시작");
        return new JobBuilder("ntceSimilarityEmptyJob", jobRepository)
                .start(ntceSimilarityEmptyStep)
                .build();
    }

    @Bean
    public Step ntceSimilarityEmptyStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
//        log.info("[JOB_CONFIG] ntceSimilarityStep 초기화 시작");
        return new StepBuilder("ntceSimilarityEmptyStep", jobRepository)
                .<List<BidNtce>, List<BidNtce>>chunk(CHUNK_SIZE, transactionManager)
                .reader(targetReader)
                .writer(ntceSimilarityEmptyWriter())
                .allowStartIfComplete(true)
                .build();
    }


    @Bean
    public ItemWriter<List<BidNtce>> ntceSimilarityEmptyWriter() {
        return new SimilarNtcesEmptyWriter(mongoTemplate, apiClient, collectionChecker);
    }
}
