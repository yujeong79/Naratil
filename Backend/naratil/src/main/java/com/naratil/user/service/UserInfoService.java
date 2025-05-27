package com.naratil.user.service;

import com.naratil.corporation.entity.Corporation;
import com.naratil.security.jwt.service.JwtService;
import com.naratil.user.dto.login.UserInfoResponseDto;
import com.naratil.user.entity.User;
import com.naratil.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserInfoService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public UserInfoResponseDto getUserInfo(String token) {

        Long userId = jwtService.getUserIdFromJwtToken(token);
        User user = userRepository.findById(userId)     //
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Corporation corporation = user.getCorporation();

        UserInfoResponseDto.UserInfoResponseDtoBuilder reponse = UserInfoResponseDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhoneNumber());

        if (corporation != null) {
            reponse
                    .bizno(corporation.getBizno())
                    .corpName(corporation.getCorpNm())
                    .ceoName(corporation.getCeoNm())
                    .openDate(corporation.getOpbizDt())
                    .employeeCount(corporation.getEmplyeNum());
        }

        return reponse.build();
    }
}
