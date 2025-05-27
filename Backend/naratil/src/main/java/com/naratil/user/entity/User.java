package com.naratil.user.entity;

import com.naratil.user.dto.signup.UserRequestDto;
import com.naratil.corporation.entity.Corporation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 11)
    private String phoneNumber;

    @Column(length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corpid")
    private Corporation corporation;

    public User(UserRequestDto userRequestDto, Corporation corporation, String password) {
        this.email = userRequestDto.email();
        this.password = password;
        this.phoneNumber = userRequestDto.phone();
        this.name = userRequestDto.name();
        this.corporation = corporation;
    }

    @Builder
    private User(UserRequestDto userRequestDto, Corporation corporation) {
        this.email = userRequestDto.email();
        this.password = userRequestDto.password();
        this.phoneNumber = userRequestDto.phone();
        this.name = userRequestDto.name();
        this.corporation = corporation;
    }

    public static User createUserWithCorp(UserRequestDto userRequestDto, String password){
        return User.builder()
                .email(userRequestDto.email())
                .password(password)
                .phoneNumber(userRequestDto.phone())
                .name(userRequestDto.name())
                .build();
    }
}
