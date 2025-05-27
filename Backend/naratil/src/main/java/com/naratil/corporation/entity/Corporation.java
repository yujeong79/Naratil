package com.naratil.corporation.entity;

import com.naratil.corporation.dto.CorpRequestDto;
import com.naratil.industry.entity.Industry;
import com.naratil.recommend.entity.Recommend;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "corporation")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Corporation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long corpid;

    @Column(nullable = false, length = 10, unique = true)
    private String bizno;

    @Column(nullable = false, length = 100)
    private String corpNm;

    private LocalDate opbizDt;

    @Column(length = 35)
    private String ceoNm;

    @Column(length = 10)
    private String emplyeNum;

    @ManyToOne
    @JoinColumn(name = "industryId")
    private Industry industry;

    @OneToMany(mappedBy = "corporation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recommend> recommends;

    // Dto에서 직접 엔티티 객체를 만들 수 있는 생성자 추가
    public Corporation(CorpRequestDto corpRequestDto, Industry industry) {
        this.bizno = corpRequestDto.businessNumber();
        this.corpNm = corpRequestDto.corpName();
        this.opbizDt = corpRequestDto.openDate();
        this.ceoNm = corpRequestDto.ceoName();
        this.emplyeNum = corpRequestDto.emplyeNum();
        this.industry = industry;
    }

    public void update(CorpRequestDto corpRequestDto, Industry industry) {
        this.corpNm = corpRequestDto.corpName();
        this.opbizDt = corpRequestDto.openDate();
        this.ceoNm = corpRequestDto.ceoName();
        this.emplyeNum = corpRequestDto.emplyeNum();
        this.industry = industry;
    }

}
