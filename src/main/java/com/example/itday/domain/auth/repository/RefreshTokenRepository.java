package com.example.itday.domain.auth.repository;

import com.example.itday.domain.auth.entity.RefreshToken;
import com.example.itday.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByMemberId(Long memberId);

    Optional<RefreshToken> findByToken(String token);

    void deleteByMemberId(Long memberId);

    Long member(Member member);
}
