package com.naratil.bid.entity;

import com.naratil.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "favorite_bid")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteBid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favId;

    private Long bidNtceId;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;
}
