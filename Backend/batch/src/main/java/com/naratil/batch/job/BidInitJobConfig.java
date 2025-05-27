package com.naratil.batch.job;

import com.naratil.batch.service.BidNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BidInitJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BidNoticeService bidNoticeService;

    @Bean
    public Job bidInitJob() {
        return new JobBuilder("bidInitJob", jobRepository)
            .start(bidInitStep())
            .build();
    }

    @Bean
    public Step bidInitStep() {
        return new StepBuilder("bidInitStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                bidNoticeService.dropBidsCollection();
                log.info("🐛 입찰공고 컬렉션 초기화 완료");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
