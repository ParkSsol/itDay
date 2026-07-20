package com.example.itday.domain.auth.dto;

public record AuthReissueResDTO(
        String accessToken,
        String refreshToken
) {}
