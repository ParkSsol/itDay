package com.example.itday.global.apiPayload.handler;

import com.example.itday.domain.auth.exception.ExpiredTokenException;
import com.example.itday.domain.auth.exception.InvalidTokenException;
import com.example.itday.domain.member.entity.Member;
import com.example.itday.domain.member.exception.MemberNotFoundException;
import com.example.itday.global.apiPayload.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidToken(InvalidTokenException e) {
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.onFailure(e.getErrorCode().getCode(), e.getErrorCode().getMessage(), null));
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredToken(ExpiredTokenException e) {
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.onFailure(e.getErrorCode().getCode(), e.getErrorCode().getMessage(), null));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleMemberNotFount(MemberNotFoundException e){
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.onFailure(e.getErrorCode().getCode(), e.getErrorCode().getMessage(), null));

    }
}