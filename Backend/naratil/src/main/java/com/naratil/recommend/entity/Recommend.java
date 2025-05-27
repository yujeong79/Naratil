package com.naratil.recommend.entity;

import com.naratil.corporation.entity.Corporation;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recommend")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Float score;

    @Column(name = "bid_ntce_id")
    private Long bidNtceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corpid")
    private Corporation corporation;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "corpid", foreignKey = @ForeignKey(name = "FKdp0ym32pbecr9weys1li60upb"))
//    private Corporation corporation;

}
