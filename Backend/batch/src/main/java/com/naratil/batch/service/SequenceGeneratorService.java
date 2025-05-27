package com.naratil.batch.service;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import com.naratil.batch.model.DatabaseSequence;
import java.util.Objects;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

/**
 * MongoDB에서 자동 증가 시퀀스를 생성하는 클래스
 * 각 엔티티 타입별로 고유한 시퀀스 값을 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SequenceGeneratorService {

    // MongoDB 작업을 수행하기 위한 Spring Data MongoDB 인터페이스
    private final MongoOperations mongoOperations;

    /**
     * 지정된 시퀀스 이름에 대한 다음 번호를 생성
     * @param seqName 시퀀스 식별자 (엔티티별로 고유한 이름)
     * @return  생성된 시퀀스 번호
     */
    public long generateSequence(String seqName) {
        // query에 해당하는 문서를 찾아 수정
        DatabaseSequence counter = mongoOperations.findAndModify(
            query(where("_id").is(seqName)),    // _id 필드가 seqName과 일치하는 문서 검색
            new Update().inc("seq", 1), // seq 필드 값을 1 증가시킴
            options().returnNew(true).upsert(true), // 수정 후의 새 문서를 반환하도록 설정. 문서가 없으면 새로 생성
            DatabaseSequence.class);    // 결과를 DatabaseSequence 객체로 매핑
        // 결과가 null이 아니면 시퀀스 값 반환, null이면 1 반환
        return !Objects.isNull(counter) ? counter.getSeq() : 1;
    }
}
