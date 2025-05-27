package com.naratil.batch.reader;

import com.naratil.batch.model.BidNotice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SimilarityTargetReaderConfig {

    private final MongoTemplate mongoTemplate;

    @Bean
    public ItemReader<List<BidNotice>> similarityTargetReader() {
        return new SimilarityTargetReader(mongoTemplate);
    }
}
