package com.naratil.industry.repository;

import com.naratil.industry.entity.Industry;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndustryRepository extends JpaRepository<Industry, Long> {

    // 검색 필터에서 사용
//    @Query("SELECT i.industryCode FROM IndustryCode i WHERE i.majorCategoryCode = :majorCategoryCode")
//    List<String> findIndustryCodesByMajorCategory(@Param("majorCategoryCode") Long majorCategoryCode);

    // 검색 필터에서 4자리에 맞춰서 List에 저장
    @Query(value = "SELECT LPAD(CAST(industry_code AS CHAR), 4, '0') FROM industry WHERE major_category_code = :majorCategoryCode", nativeQuery = true)
    List<String> findIndustryCodesByMajorCategory(@Param("majorCategoryCode") Long majorCategoryCode);

    Optional<Industry> findByIndustryCode(Long industryCode);

}
