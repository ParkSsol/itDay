package com.example.itday.domain.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoTokenResDTO(
        @JsonProperty("access_token")
        String accessToken
) {}
