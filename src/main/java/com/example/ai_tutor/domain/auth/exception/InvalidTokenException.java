package com.example.ai_tutor.domain.auth.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException() {
        super("이미 로그아웃 된 유저입니다.");
    }

}