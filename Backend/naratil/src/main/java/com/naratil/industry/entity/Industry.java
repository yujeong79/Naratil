package com.naratil.industry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "industry")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Industry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // IDENTITY → SEQUENCE 변경
    private Long id;

    @Column(length = 2, nullable = false)
    private Long majorCategoryCode;   // 대분류Id

    @Column(nullable = false)
    private String majorCategoryName;   // 대분류

    @Column(length = 2, nullable = false)
    private Long categoryCode;  // 분류 코드 2자리

    @Column(nullable = false)
    private String categoryName;    // 분류

    @Column(length = 4, nullable = false)
    private Long industryCode;  // 업종 코드

    @Column(length = 200, nullable = false)
    private String industryName;    // 업종 명
}
