package com.naratil.batch.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

/**
 * MongoDB 연결 및 설정 관리하는 클래스
 */
@Configuration
@RequiredArgsConstructor
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    /**
     * AbstractMongoClientConfiguration에서 요구하는 메서드
     *
     * @return 사용할 MongoDB 데이터베이스 이름
     */
    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    /**
     * MongoDB 클라이언트 구성 및 생성
     */
    @Override
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);

        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            // 소켓 연결 및 읽기 타임아웃 설정
            .applyToSocketSettings(builder -> builder
                .connectTimeout(5000, TimeUnit.MILLISECONDS)    // 연결 타임아웃 : 5초
                .readTimeout(15000, TimeUnit.MILLISECONDS)) // 읽기 타임아웃 : 15초
            // 커넥션 풀 설정
            .applyToConnectionPoolSettings(builder -> builder
                .maxWaitTime(10000, TimeUnit.MILLISECONDS)  // 최대 대기 시간 : 10초
                .maxSize(50)    // 최대 커넥션 수 : 50개
                .minSize(5))    // 최소 커넥션 수 : 5개
            .build();

        return MongoClients.create(mongoClientSettings);
    }

    /**
     * MongoDB 작업을 위한 MongoTemplate Bean 생성 MongoDB 문서와 Java 객체 간의 매핑 처리
     */
    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }

    /**
     * MongoDB 트랜잭션 관리자 Bean 생성 트랜잭션 기능을 사용할 수 있게 함
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory databaseFactory) {
        return new MongoTransactionManager(databaseFactory);
    }

    /**
     * MongoDB 문서와 Java 객체 간 변환을 담당하는 Converter Bean 생성
     */
    @Bean
    public MappingMongoConverter mappingMongoConverter(
        MongoDatabaseFactory databaseFactory,
        MongoCustomConversions customConversions,
        MongoMappingContext mappingContext) {

        MappingMongoConverter converter = super.mappingMongoConverter(databaseFactory,
            customConversions, mappingContext);

        // MongoDB에 저장될 때 '_class' 필드를 제거
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        return converter;
    }
}
