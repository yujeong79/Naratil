package com.naratil.batch.job;

import com.naratil.batch.client.fastapi.SimilarityApiClient;
import com.naratil.batch.common.MongoCollectionChecker;
import com.naratil.batch.model.BidNotice;
import com.naratil.batch.writer.SimilarNtcesWriter;
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
public class NtceSimilarityJobConfig {

    private final MongoTemplate mongoTemplate;
    private final MongoCollectionChecker collectionChecker;
    private final SimilarityApiClient apiClient;
    private final ItemReader<List<BidNotice>> targetReader;

    private final static int CHUNK_SIZE = 10;

    @Bean
    public Job ntceSimilarityJob(JobRepository jobRepository, Step ntceSimilarityStep) {
//        log.info("[JOB_CONFIG] ntceSimilarityJob 초기화 시작");
        return new JobBuilder("ntceSimilarityJob", jobRepository)
                .start(ntceSimilarityStep)
                .build();
    }

    @Bean
    public Step ntceSimilarityStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
//        log.info("[JOB_CONFIG] ntceSimilarityStep 초기화 시작");
        return new StepBuilder("ntceSimilarityStep", jobRepository)
                .<List<BidNotice>, List<BidNotice>>chunk(CHUNK_SIZE, transactionManager)
                .reader(targetReader)
                .writer(ntceSimilarityWriter())
                .allowStartIfComplete(true)
                .build();
    }


    @Bean
    public ItemWriter<List<BidNotice>> ntceSimilarityWriter() {
        return new SimilarNtcesWriter(mongoTemplate, apiClient, collectionChecker);
    }
}
