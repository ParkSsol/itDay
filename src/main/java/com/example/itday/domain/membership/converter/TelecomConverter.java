package com.example.itday.domain.membership.converter;

import com.example.itday.domain.membership.enums.Telecom;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

/*
* Java의 Telecom.LGU <-> DB의 'LG U+' 문자열을 서로 변환하는 컨버터.
* DB enum 값 중 'LG U+'는 공백/특수문자가 있어 Java enum 상수명으로 쓸 수 없어서 필요함.
* */

@Converter(autoApply = true)
public class TelecomConverter implements AttributeConverter<Telecom, String> {

    // JAVA -> DB 변환
    @Override
    public String convertToDatabaseColumn(Telecom telecom) {
        if (telecom == null) {
            return null;
        }
        return telecom.getLabel();
    }

    // DB -> JAVA 변환
    @Override
    public Telecom convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        return Arrays.stream(Telecom.values())
                .filter(telecom -> telecom.getLabel().equals(dbValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 telecom : " + dbValue));
    }

}
