package com.naratil.batch.reader;

import com.naratil.batch.dto.fastapi.common.NtceBase;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@Configuration
public class NtceEmbeddingReaderConfig {

    @Bean
    public ItemReader<List<NtceBase>> ntceEmbeddingReader(MongoTemplate mongoTemplate) {
        return new NtceEmbeddingReader(mongoTemplate);
    }
}
