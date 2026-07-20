package com.example.itday.domain.auth.dto;

public record KakaoLoginResDTO(
        String accessToken,
        String refreshToken,
        Long userId,
        boolean isNewUser
) {}
