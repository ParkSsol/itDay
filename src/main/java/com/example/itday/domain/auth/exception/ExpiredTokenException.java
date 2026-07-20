package com.example.itday.domain.auth.exception;

import com.example.itday.global.apiPayload.code.ErrorCode;
import lombok.Getter;

@Getter
public class ExpiredTokenException extends RuntimeException {
    private final ErrorCode errorCode;

    public ExpiredTokenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
