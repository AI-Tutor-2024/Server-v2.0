package com.example.ai_tutor.global.exception;

import com.example.ai_tutor.global.payload.ErrorCode;

public class GeneralException extends DefaultException {

    public GeneralException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    public GeneralException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
