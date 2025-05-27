package com.naratil.batch.step;

import com.naratil.batch.tasklet.ClearCurrentIndexTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * 현재 공고 FAISS 인덱싱 관련 StepConfig
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CurrentIndexStepConfig {

    private final ClearCurrentIndexTasklet clearCurrentIndexTasklet;

    /**
     * 현재 인덱스 초기화
     * @param jobRepository -
     * @param transactionManager -
     * @return -
     */
    @Bean
    public Step clearCurrentIndexStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        log.info("[JOB_CONFIG] clearCurrentIndexStep 초기화");
        return new StepBuilder("clearCurrentIndexStep", jobRepository)
                .tasklet(clearCurrentIndexTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    /**
     * 현재 인덱스 생성/추가
     * @param jobRepository -
     * @param transactionManager -
     * @return -
     */
    @Bean
    public Step createCurrentIndexStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<List<Document>> createCurrentIndexReader,
            ItemWriter<List<Document>> createCurrentIndexWriter
    ) {
        //        log.info("[JOB_CONFIG] createCurrentIndexStep 초기화");
        return new StepBuilder("createCurrentIndexStep", jobRepository)
                .<List<Document>, List<Document>>chunk(50000, transactionManager)
                .reader(createCurrentIndexReader)
                .writer(createCurrentIndexWriter)
                .allowStartIfComplete(true)
                .build();
    }

}
