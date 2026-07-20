package com.example.itday.domain.member.controller;

import com.example.itday.domain.member.service.MemberService;
import com.example.itday.global.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @DeleteMapping("/me")
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal Long memberId){
        memberService.withdraw(memberId);
        return ApiResponse.onSuccess("회원 탈퇴 되었습니다", null);
    }
}
