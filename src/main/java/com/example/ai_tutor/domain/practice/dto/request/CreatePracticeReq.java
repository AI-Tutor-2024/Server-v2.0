package com.example.ai_tutor.domain.practice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class CreatePracticeReq {

    // 문제 개수
    @Schema(type = "int", example ="10", description="생성할 문제의 개수입니다. 0일 경우 AI 추천(10개)")
    private int practiceSize;

    // 문제 유형
    @Schema( type = "String", example ="OX", description="생성할 문제의 유형입니다. OX(OX문제), MULTIPLE(객관식), BOTH(혼합)")
    private String type;
}
