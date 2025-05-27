package com.naratil.batch.step;

import com.naratil.batch.tasklet.UpdatePastIndexTasklet;
import com.naratil.batch.tasklet.ClearPastIndexTasklet;
import com.naratil.batch.tasklet.CreatePastIndexTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 과거 공고 FAISS 인덱싱 관련 StepConfig
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class PastIndexStepConfig {

    private final ClearPastIndexTasklet clearPastIndexTasklet;
    private final CreatePastIndexTasklet createPastIndexTasklet;
    private final UpdatePastIndexTasklet updatePastIndexTasklet;

    /**
     * 과거 인덱스 초기화
     * @param jobRepository -
     * @param transactionManager -
     * @return -
     */
    @Bean
    public Step clearPastIndexStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("clearPastIndexStep", jobRepository)
                .tasklet(clearPastIndexTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    /**
     * 과거 인덱스 생성
     * @param jobRepository -
     * @param transactionManager -
     * @return -
     */
    @Bean
    public Step createPastIndexStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("createPastIndexStep", jobRepository)
                .tasklet(createPastIndexTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }

    /**
     * 과거 mongoDB에 키워드 업데이트 및 키워드/벡터 인덱스에 추가
     * @param jobRepository -
     * @param transactionManager -
     * @return -
     */
    @Bean
    public Step updatePastIndexStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("updatePastIndexStep", jobRepository)
                .tasklet(updatePastIndexTasklet, transactionManager)
                .allowStartIfComplete(true)
                .build();
    }
}
