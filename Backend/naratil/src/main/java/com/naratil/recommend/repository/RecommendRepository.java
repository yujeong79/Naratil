package com.naratil.recommend.repository;

import com.naratil.recommend.entity.Recommend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RecommendRepository extends JpaRepository<Recommend, Long> {

    @Query(value = """
        SELECT r.*
        FROM recommend r
        JOIN corporation c ON c.corpid = r.corpid
        JOIN user u ON u.corpid = c.corpid
        WHERE u.id = :userId
        """, nativeQuery = true)
    List<Recommend> findRecommendsByUserId(long userId);

}
