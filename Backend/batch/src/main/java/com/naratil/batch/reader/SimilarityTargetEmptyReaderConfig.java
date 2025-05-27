package com.naratil.batch.reader;

import com.naratil.batch.dto.fastapi.similarity.BidNtce;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SimilarityTargetEmptyReaderConfig {

    private final MongoTemplate mongoTemplate;

    @Bean
    public ItemReader<List<BidNtce>> similarityTargetEmptyReader() {
        return new SimilarityTargetEmptyReader(mongoTemplate);
    }
}
