package com.example.itday.domain.member.service;

import com.example.itday.domain.auth.repository.RefreshTokenRepository;
import com.example.itday.domain.auth.service.KakaoAuthClient;
import com.example.itday.domain.member.entity.Member;
import com.example.itday.domain.member.exception.MemberNotFoundException;
import com.example.itday.domain.member.repository.MemberRepository;
import com.example.itday.global.apiPayload.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KakaoAuthClient kakaoAuthClient;

    @Transactional
    public void withdraw(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()-> new MemberNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        kakaoAuthClient.unlink(member.getSocialId());
        refreshTokenRepository.deleteByMemberId(memberId);
        memberRepository.delete(member);
    }
}
