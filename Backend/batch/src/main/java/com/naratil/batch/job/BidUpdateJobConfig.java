package com.naratil.batch.job;

import com.naratil.batch.dto.nara.BidUpdateItem;
import com.naratil.batch.dto.nara.response.BasicPriceApiResponse.BasicPriceItem;
import com.naratil.batch.dto.nara.response.LicenseLimitApiResponse.LicenseLimitItem;
import com.naratil.batch.dto.nara.response.RegionLimitApiResponse.RegionLimitItem;
import com.naratil.batch.model.BidNotice;
import com.naratil.batch.processor.BidBasicPriceItemProcessor;
import com.naratil.batch.processor.BidLicenseLimitItemProcessor;
import com.naratil.batch.processor.BidRegionLimitItemProcessor;
import com.naratil.batch.processor.BidUpdateItemProcessor;
import com.naratil.batch.reader.BidLicenseLimitItemReader;
import com.naratil.batch.reader.BidPriceNoItemReader;
import com.naratil.batch.reader.BidRegionLimitItemReader;
import com.naratil.batch.reader.BidUpdateItemReader;
import com.naratil.batch.service.BidNoticeService;
import com.naratil.batch.writer.BidNoticeMongoItemWriter;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 수집된 공고에서 최신 차수만 저장하고, 면허제한정보와 참가가능지역정보를 수집하는 Job 정의
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class BidUpdateJobConfig {

    // 청크 크기 (한 번에 처리할 아이템 수)
    private static final int CHUNK_SIZE = 100;
    // 입찰 공고 데이터를 저장할 MongoDB 컬렉션 이름
    private static final String COLLECTION_NAME = "bids";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BidNoticeService bidNoticeService;
    private final MongoTemplate mongoTemplate;

    @Bean
    public Job bidUpdateJob() {
        return new JobBuilder("bidUpdateJob", jobRepository)
            .start(cleanupDuplicateBidStep())   // 공고 중복 제거 Step
//            .next(updateBidStep())              // 공고번호로 정보 업데이트 Step
            .next(updatePriceStep())
            .next(updateLicenseStep())
            .next(updateRegionStep())
            .next(markUpdatedBidStep())         // 처리 완료된 공고 isUpdated를 true로 변경
            .build();
    }

    /**
     * bidNtceNo 중복 공고를 제거하는 Step
     */
    @Bean
    public Step cleanupDuplicateBidStep() {
        return new StepBuilder("cleanupDuplicateBidStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                bidNoticeService.cleanupDuplicateBid();
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean
    public Step updateBidStep() {
        return new StepBuilder("updateBidStep", jobRepository)
            .<BidUpdateItem, BidUpdateItem>chunk(CHUNK_SIZE, transactionManager)
            .reader(bidUpdateItemReader())
            .processor(bidUpdateItemProcessor())
            .writer(bidUpdateItemWriter())
            .build();
    }

    @Bean
    public Step updatePriceStep() {
        return new StepBuilder("updatePriceStep", jobRepository)
            .<BasicPriceItem, BasicPriceItem>chunk(CHUNK_SIZE, transactionManager)
            .reader(updatePriceReader())
            .processor(updatePriceProcessor())
            .writer(updatePriceWriter())
            .build();
    }

    @Bean
    public Step updateLicenseStep() {
        return new StepBuilder("updateLicenseStep", jobRepository)
            .<LicenseLimitItem, LicenseLimitItem>chunk(CHUNK_SIZE, transactionManager)
            .reader(updateLicenseReader())
            .processor(updateLicenseProcessor())
            .writer(updateLicenseWriter())
            .build();
    }

    @Bean
    public Step updateRegionStep() {
        return new StepBuilder("updateRegionStep", jobRepository)
            .<RegionLimitItem, RegionLimitItem>chunk(CHUNK_SIZE, transactionManager)
            .reader(updateRegionReader())
            .processor(updateRegionProcessor())
            .writer(updateRegionWriter())
            .build();
    }

    @Bean
    public Step markUpdatedBidStep() {
        return new StepBuilder("markUpdatedBidStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                long modifiedCount = bidNoticeService.updateNotUpdatedBid();
                log.info("🐛 {}개 업데이트 완료", modifiedCount);
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }

    @Bean
    public BidUpdateItemReader bidUpdateItemReader() {
        return new BidUpdateItemReader(bidNoticeService);
    }

    @Bean
    public BidUpdateItemProcessor bidUpdateItemProcessor() {
        return new BidUpdateItemProcessor(bidNoticeService);
    }

    @Bean
    public ItemWriter<BidUpdateItem> bidUpdateItemWriter() {
        return items -> {
            int count = items.size();
            log.debug("🐛 입찰공고 업데이트 완료: {}건", count);
        };
    }

    @Bean
    public BidPriceNoItemReader updatePriceReader() {
        return new BidPriceNoItemReader(bidNoticeService);
    }

    @Bean
    public BidBasicPriceItemProcessor updatePriceProcessor() {
        return new BidBasicPriceItemProcessor(bidNoticeService);
    }

    @Bean
    public ItemWriter<BasicPriceItem> updatePriceWriter() {
        return items -> {
            int count = items.size();
            log.debug("🐛 입찰공고 기초금액 업데이트 완료: {}건", count);
        };
    }

    @Bean
    public BidLicenseLimitItemReader updateLicenseReader() {
        return new BidLicenseLimitItemReader(bidNoticeService);
    }

    @Bean
    public BidLicenseLimitItemProcessor updateLicenseProcessor() {
        return new BidLicenseLimitItemProcessor(bidNoticeService);
    }

    @Bean
    public ItemWriter<LicenseLimitItem> updateLicenseWriter() {
        return items -> {
            int count = items.size();
            log.debug("🐛 입찰공고 면허제한정보 업데이트 완료: {}건", count);
        };
    }

    @Bean
    public BidRegionLimitItemReader updateRegionReader() {
        return new BidRegionLimitItemReader(bidNoticeService);
    }

    @Bean
    public BidRegionLimitItemProcessor updateRegionProcessor() {
        return new BidRegionLimitItemProcessor(bidNoticeService);
    }

    @Bean
    public ItemWriter<RegionLimitItem> updateRegionWriter() {
        return items -> {
            int count = items.size();
            log.debug("🐛 입찰공고 참가가능지역정보 업데이트 완료: {}건", count);
        };
    }

}
