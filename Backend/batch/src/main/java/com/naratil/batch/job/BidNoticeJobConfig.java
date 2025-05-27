package com.naratil.batch.job;

import com.naratil.batch.dto.nara.response.BidNoticeApiResponse.BidNoticeItem;
import com.naratil.batch.model.BidNotice;
import com.naratil.batch.processor.BidNoticeItemProcessor;
import com.naratil.batch.reader.BidNoticeDateItemReader;
import com.naratil.batch.service.BidNoticeService;
import com.naratil.batch.service.SequenceGeneratorService;
import com.naratil.batch.writer.BidNoticeMongoItemWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 현재 입찰중인 공고 데이터 수집을 위한 Job 설정 클래스 공공데이터 포털 API로 입찰공고 정보를 가져와 MongoDB에 저장하는 Job을 정의
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BidNoticeJobConfig {

    // 청크 크기 (한 번에 처리할 아이템 수)
    private static final int CHUNK_SIZE = 100;
    // 입찰 공고 데이터를 저장할 MongoDB 컬렉션 이름
    private static final String COLLECTION_NAME = "bids";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BidNoticeService bidNoticeService;
    private final MongoTemplate mongoTemplate;
    private final SequenceGeneratorService sequenceGenerator;

    /**
     * 입찰공고 수집 Job 정의
     */
    @Bean
    public Job bidNoticeJob() {
        return new JobBuilder("bidNoticeJob", jobRepository)
            .start(bidThingStep())      // 물품 입찰 공고 수집 Step
            .next(bidServiceStep())     // 용역 입찰 공고 수집 Step
            .next(bidConstructionStep())// 공사 입찰 공고 수집 Step
            .build();
    }

    /**
     * 입찰공고 물품 수집 Step
     */
    @Bean
    public Step bidThingStep() {
        return new StepBuilder("bidThingStep", jobRepository)
            .<BidNoticeItem, BidNotice>chunk(CHUNK_SIZE, transactionManager)    // 청크 기반 처리 설정
            .reader(bidThingItemReader(null, null)) // API 데이터를 읽는 Reader
            .processor(bidThingItemProcessor())    // 데이터를 변환하는 Processor
            .writer(bidNoticeItemWriter())  // MongoDB에 저장하는 Writer
            .build();
    }

    /**
     * 입찰공고 용역 수집 Step
     */
    @Bean
    public Step bidServiceStep() {
        return new StepBuilder("bidServiceStep", jobRepository)
            .<BidNoticeItem, BidNotice>chunk(CHUNK_SIZE, transactionManager)
            .reader(bidServiceItemReader(null, null))
            .processor(bidServiceItemProcessor())
            .writer(bidNoticeItemWriter())
            .build();
    }

    /**
     * 입찰공고 공사 수집 Step
     */
    @Bean
    public Step bidConstructionStep() {
        return new StepBuilder("bidConstructionStep", jobRepository)
            .<BidNoticeItem, BidNotice>chunk(CHUNK_SIZE, transactionManager)
            .reader(bidConstructionItemReader(null, null))
            .processor(bidConstructionItemProcessor())
            .writer(bidNoticeItemWriter())
            .build();
    }

    /**
     * API에서 입찰공고 물품 BidNoticeItem을 읽어오는 Reader 정의
     */
    @Bean
    @StepScope  // Step 실행 시점에 Bean이 생성되도록 설정
    public BidNoticeDateItemReader bidThingItemReader(
        // Step 실행 시 jobParameters에서 날짜 주입
        @Value("#{jobParameters['startDate']}") String startDate,
        @Value("#{jobParameters['endDate']}") String endDate) {
        return new BidNoticeDateItemReader(bidNoticeService, "물품", startDate, endDate);
    }

    /**
     * API에서 입찰공고 용역 BidNoticeItem을 읽어오는 Reader 정의
     */
    @Bean
    @StepScope
    public BidNoticeDateItemReader bidServiceItemReader(
        @Value("#{jobParameters['startDate']}") String startDate,
        @Value("#{jobParameters['endDate']}") String endDate) {
        return new BidNoticeDateItemReader(bidNoticeService, "용역", startDate, endDate);
    }


    /**
     * API에서 입찰공고 공사 BidNoticeItem을 읽어오는 Reader 정의
     */
    @Bean
    @StepScope
    public BidNoticeDateItemReader bidConstructionItemReader(
        @Value("#{jobParameters['startDate']}") String startDate,
        @Value("#{jobParameters['endDate']}") String endDate) {
        return new BidNoticeDateItemReader(bidNoticeService, "공사", startDate, endDate);
    }

    /**
     * 입찰공고 물품 BidNoticeItem을 BidNotice 객체로 변환하는 Processor 정의
     */
    @Bean
    public BidNoticeItemProcessor bidThingItemProcessor() {
        return new BidNoticeItemProcessor(sequenceGenerator, "물품");
    }

    /**
     * 입찰공고 용역 BidNoticeItem을 BidNotice 객체로 변환하는 Processor 정의
     */
    @Bean
    public BidNoticeItemProcessor bidServiceItemProcessor() {
        return new BidNoticeItemProcessor(sequenceGenerator, "용역");
    }

    /**
     * 입찰공고 공사 BidNoticeItem을 BidNotice 객체로 변환하는 Processor 정의
     */
    @Bean
    public BidNoticeItemProcessor bidConstructionItemProcessor() {
        return new BidNoticeItemProcessor(sequenceGenerator, "공사");
    }
    
    /**
     * 처리된 입찰공고 데이터를 MongoDB에 저장하는 Writer 정의
     */
    @Bean
    public BidNoticeMongoItemWriter bidNoticeItemWriter() {
        return new BidNoticeMongoItemWriter(mongoTemplate, COLLECTION_NAME);
    }

}
