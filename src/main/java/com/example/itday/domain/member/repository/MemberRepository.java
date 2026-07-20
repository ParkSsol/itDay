package com.example.itday.domain.member.repository;

import com.example.itday.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

    Optional<Member> findBySocialId(Long socialId);

    Optional<Member> findById(Long memberId);

}
