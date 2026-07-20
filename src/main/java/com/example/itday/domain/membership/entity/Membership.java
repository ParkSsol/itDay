package com.example.itday.domain.membership.entity;

import com.example.itday.domain.membership.enums.Telecom;
import com.example.itday.domain.membership.enums.TelecomGrade;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "membership")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "membershipId")
    private Long id;

    @Column(name = "telecomGrade", nullable = false)
    @Enumerated(EnumType.STRING)
    private TelecomGrade telecomGrade;

    @Column(name = "telecom", nullable = false)
    private Telecom telecom;

    @Column(name = "gradeContent")
    private String gradeContent;
}
