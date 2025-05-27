package com.naratil.batch.job;

import com.naratil.batch.dto.fastapi.common.NtceBase;
import com.naratil.batch.model.BidNotice;
import com.naratil.batch.processor.NtceVectorProcessor;
import com.naratil.batch.reader.NtceEmbeddingReader;
import com.naratil.batch.writer.NtceEmbeddingBulkWriter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

/**
 * 공고 벡터 임베딩 배치 Job
 * - Mongo에서 100건 단위로 공고 읽기
 * - FastAPI 호출
 * - 키워드 추출 후 MongoDB에 Bulk Update
 *
 * Reader: Stream 커서
 * Processor: 병렬, 파티셔닝
 * Writer: 병렬
 */
@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class NtceEmbeddingJobConfig {

    private final NtceVectorProcessor ntceVectorProcessor;
    private final NtceEmbeddingBulkWriter ntceEmbeddingWriter;
    private final MongoTemplate mongoTemplate;


    @Bean
    public Job ntceEmbeddingJob(JobRepository jobRepository, Step ntceEmbeddingStep) {
//        log.info("[JOB_CONFIG] ntceEmbeddingJob 초기화");
        return new JobBuilder("ntceEmbeddingJob", jobRepository)
                .start(ntceEmbeddingStep)
                .build();
    }

    /**
     * Step 정의: 한 번에 500건 리스트 처리
     * chunk(1) = List<NtceBase> 1개 처리
     */
    @Bean
    public Step ntceEmbeddingStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<List<NtceBase>> ntceEmbeddingReader,
            ItemProcessor<List<NtceBase>, List<BidNotice>> ntceEmbeddingProcessor,
            ItemWriter<List<BidNotice>> embeddingWriter
    ) {
//        log.info("[JOB_CONFIG] ntceEmbeddingStep 구성 시작");
        return new StepBuilder("ntceEmbeddingStep", jobRepository)
                .<List<NtceBase>, List<BidNotice>>chunk(1, transactionManager)
                .reader(ntceEmbeddingReader)
                .processor(ntceEmbeddingProcessor)
                .writer(embeddingWriter)
                .faultTolerant()
                .skip(Exception.class) // 개별 처리 실패는 skip
                .skipLimit(10) // 최대 10번 skip 허용
                .allowStartIfComplete(true)
                .build();
    }

    /**
     * Reader: Mongo에서 벡터화 안 된 공고  읽기
     */
    @Bean
    public ItemReader<List<NtceBase>> embeddingReader() {
//        log.info("[JOB_CONFIG] embeddingReader Bean 등록");
        return new NtceEmbeddingReader(mongoTemplate);
    }

    /**
     * Processor: FastAPI 호출 + 키워드 추출 : 최대 100건씩 벡터화 요청 + 최대 5건 병렬 호출 (500건 처리)
     */
    @Bean
    public ItemProcessor<List<NtceBase>, List<BidNotice>> ntceEmbeddingProcessor() {
//        log.info("[JOB_CONFIG] ntceEmbeddingProcessor Bean 등록");
        return ntceVectorProcessor;
    }

    /**
     * Writer: MongoDB Bulk Update
     */
    @Bean
    public ItemWriter<List<BidNotice>> embeddingWriter() {
//        log.info("[JOB_CONFIG] ntceEmbeddingWriter Bean 등록(embeddingWriter)");
        return ntceEmbeddingWriter;
    }
}
