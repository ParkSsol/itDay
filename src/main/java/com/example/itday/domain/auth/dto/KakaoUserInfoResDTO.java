package com.example.itday.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResDTO(
    Long id,
    @JsonProperty("kakao_account")
    KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            String email,
            String name,
            String birthday,
            String birthyear,
            @JsonProperty("phone_number")
            String phoneNumber
    ){}
}
