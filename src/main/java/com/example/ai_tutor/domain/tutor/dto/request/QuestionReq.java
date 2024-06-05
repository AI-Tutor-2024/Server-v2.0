package com.example.ai_tutor.domain.tutor.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class QuestionReq {

    @Schema( type = "String", example ="기말고사 잘 보는 방법 알려줘", description="질문 내용입니다.")
    public String question;
}
