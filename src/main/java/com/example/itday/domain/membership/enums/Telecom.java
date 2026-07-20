package com.example.itday.domain.membership.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Telecom {
    KT("KT"),
    SKT("SKT"),
    LGU("LG U+");

    private final String label;
}
