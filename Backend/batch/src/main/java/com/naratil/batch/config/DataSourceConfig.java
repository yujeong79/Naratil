package com.naratil.batch.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 데이터 소스 설정 클래스
 * Spring Batch 애플리케이션에서 사용하는 여러 데이터 소스를 정의하고 관리
 */
@Configuration
public class DataSourceConfig {

    /**
     * 배치 메타데이터 저장에 사용되는 데이터 소스 정의
     */
    @Bean(name = {"dataSource", "batchDataSource"})
    @Primary    // 명시적으로 지정하지 않았을 때 기본적으로 사용되는 데이터 소스로 지정
    @ConfigurationProperties(prefix = "spring.datasource.batch")
    public DataSource batchDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 비즈니스 로직에 사용되는 주 데이터 소스 정의
     */
    @Bean(name = "businessDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.business")
    public DataSource businessDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * batchDataSource를 위한 트랜잭션 매니저 정의
     */
    @Bean
    @Primary
    public PlatformTransactionManager batchTransactionManager(
        @Qualifier("batchDataSource") DataSource batchDataSource) {
        return new JdbcTransactionManager(batchDataSource);
    }

    /**
     * businessDataSource를 위한 트랜잭션 매니저 정의
     */
    @Bean
    public PlatformTransactionManager businessTransactionManager(
        @Qualifier("businessDataSource") DataSource businessDataSource) {
        return new JdbcTransactionManager(businessDataSource);
    }

    @Bean(name = "businessJdbcTemplate")
    public JdbcTemplate businessJdbcTemplate(
            @Qualifier("businessDataSource") DataSource businessDataSource
    ) {
        return new JdbcTemplate(businessDataSource);
    }


}
