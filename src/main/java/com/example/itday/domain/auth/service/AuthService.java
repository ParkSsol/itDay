package com.example.itday.domain.auth.service;

import com.example.itday.domain.auth.dto.AuthReissueResDTO;
import com.example.itday.domain.auth.dto.KakaoLoginResDTO;
import com.example.itday.domain.auth.dto.KakaoUserInfoResDTO;
import com.example.itday.domain.auth.entity.RefreshToken;
import com.example.itday.domain.auth.exception.ExpiredTokenException;
import com.example.itday.domain.auth.exception.InvalidTokenException;
import com.example.itday.domain.auth.repository.RefreshTokenRepository;
import com.example.itday.domain.member.entity.Member;
import com.example.itday.domain.member.repository.MemberRepository;
import com.example.itday.global.apiPayload.code.ErrorCode;
import com.example.itday.global.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoAuthClient kakaoAuthClient;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public KakaoLoginResDTO kakaoLogin(String code){

        // code -> kakaoAccessToken
        String kakaoAccessToken = kakaoAuthClient.getAccessToken(code);

        // 카카오 kakaoAccessToken -> 사용자 정보 받기
        KakaoUserInfoResDTO userInfo = kakaoAuthClient.getUserInfo(kakaoAccessToken);

        // 회원 존재 여부 조회
        Optional<Member> existingMember = memberRepository.findBySocialId(userInfo.id());

        boolean isNewUser = existingMember.isEmpty();

        // 신규 회원이면 저장, 기존 회원이면 그대로
        Member member = existingMember.orElseGet(() -> {
            Member newMember = Member.builder()
                    .socialId(userInfo.id())
                    .email(userInfo.kakaoAccount().email())
                    .name(userInfo.kakaoAccount().name())
                    .phone(userInfo.kakaoAccount().phoneNumber())
                    .birth(toBirthDate(userInfo.kakaoAccount().birthyear(),userInfo.kakaoAccount().birthday()))
                    .createdAt(LocalDateTime.now())
                    .build();

            return memberRepository.save(newMember);
        });

        // JWT 발급
        String accessToken = jwtUtil.createAccessToken(member.getId());
        String refreshTokenValue = jwtUtil.createRefreshToken(member.getId());

        // refreshToken upsert
        saveOrUpdateRefreshToken(member,refreshTokenValue);

        return new KakaoLoginResDTO(accessToken,refreshTokenValue,member.getId(),isNewUser);
    }

    // access/refresh Token 재발급
    @Transactional
    public AuthReissueResDTO reissue(String refreshTokenValue){

        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(()-> new InvalidTokenException(ErrorCode.INVALID_TOKEN));
        if(savedToken.getExpiresAt().isBefore(LocalDateTime.now())){
            refreshTokenRepository.delete(savedToken);
            throw new ExpiredTokenException(ErrorCode.EXPIRED_TOKEN);
        }

        Long memberId = savedToken.getMemberId();

        String accessToken = jwtUtil.createAccessToken(memberId);
        String refreshToken = jwtUtil.createRefreshToken(memberId);

        savedToken.setToken(refreshToken);
        savedToken.setExpiresAt(LocalDateTime.now().plusDays(14));

        return new AuthReissueResDTO(accessToken,refreshToken);
    }

    @Transactional
    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }


    // refreshToken을 DB에 upsert
    private void saveOrUpdateRefreshToken(Member member, String tokenValue){
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(14);

        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        existing-> {
                            existing.setToken(tokenValue);
                            existing.setExpiresAt(expiresAt);
                        },
                        ()-> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .member(member)
                                        .token(tokenValue)
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );
    }

    // 카카오가 반환한 birthyear, birthday 합쳐 생년월일 만들기
    private LocalDate toBirthDate(String birthyear, String birthday){
        if (birthyear == null || birthday == null) {
            return null;   // 정보가 없으면 그냥 null을 반환
        }
        int year = Integer.parseInt(birthyear);
        int month = Integer.parseInt(birthday.substring(0, 2));
        int day = Integer.parseInt(birthday.substring(2, 4));
        return LocalDate.of(year, month, day);
    }
}
