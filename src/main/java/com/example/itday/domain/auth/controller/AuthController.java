package com.example.itday.domain.auth.controller;

import com.example.itday.domain.auth.dto.AuthReissueReqDTO;
import com.example.itday.domain.auth.dto.AuthReissueResDTO;
import com.example.itday.domain.auth.dto.KakaoLoginReqDTO;
import com.example.itday.domain.auth.dto.KakaoLoginResDTO;
import com.example.itday.domain.auth.service.AuthService;
import com.example.itday.global.apiPayload.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @GetMapping("/oauth/social")
    public void redirectToKakao(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = "https://kauth.kakao.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=account_email,name,birthday,birthyear,phone_number";
        response.sendRedirect(kakaoAuthUrl);
    }

    @PostMapping("/oauth/social/callback")
    public ApiResponse<KakaoLoginResDTO> kakaoCallback(@RequestBody KakaoLoginReqDTO request) {
        KakaoLoginResDTO result = authService.kakaoLogin(request.code());
        return ApiResponse.onSuccess(result);
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<AuthReissueResDTO> reissue(@RequestBody AuthReissueReqDTO request) {
        AuthReissueResDTO result = authService.reissue(request.refreshToken());
        return ApiResponse.onSuccess("성공적으로 재발급 되었습니다",result);
    }

    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long memberId){
        authService.logout(memberId);
        return ApiResponse.onSuccess("로그아웃 되었습니다", null);
    }
}
