package com.example.ai_tutor.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePracticeRes {

    @Schema(type = "int", example ="1", description="문제의 번호입니다.")
    private int practiceNumber;

    @Schema(type = "String", example ="광복절의 중앙경축식은 매년 서울에서만 거행된다.", description="문제의 내용입니다.")
    private String content;

    // 문제 문항(객관식 문제의 경우 사용)
    @ArraySchema(schema = @Schema(type = "String", example ="A. 1945년", description="문제의 문항으로, 객관식 문제에만 해당합니다."))
    private List<String> choices;

    // 문제 답안
    @Schema(type = "String", example ="X",
            description="문제의 답안입니다. OX문제의 경우 O 또는 X, 객관식 문제의 경우 A, B, C, D입니다.")
    private String result;

    @Schema(type = "String", example ="요약문에 따르면 중앙경축식은 서울에서 거행되고, 지방경축행사는 각 시·도 단위별로 거행된다.", description="문제의 해설입니다.")
    private String solution;

    // 문제 타입
    @Schema(type = "String", example ="OX", description="문제의 타입입니다. OX, MULTIPLE")
    private String practiceType;
}
