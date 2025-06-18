package com.example.ai_tutor.global.payload;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, null, "잘못된 요청 데이터 입니다."),
    INVALID_REPRESENTATION(HttpStatus.BAD_REQUEST, null, "잘못된 표현 입니다."),
    INVALID_FILE_PATH(HttpStatus.BAD_REQUEST, null, "잘못된 파일 경로 입니다."),
    INVALID_OPTIONAL_IS_PRESENT(HttpStatus.BAD_REQUEST, null, "해당 값이 존재하지 않습니다."),
    INVALID_CHECK(HttpStatus.BAD_REQUEST, null, "해당 값이 유효하지 않습니다."),
    INVALID_AUTHENTICATION(HttpStatus.BAD_REQUEST, null, "잘못된 인증입니다."),
    PRACTICE_NOT_FOUND(HttpStatus.NOT_FOUND, null, "");

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(final HttpStatus status, final String code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }

}
