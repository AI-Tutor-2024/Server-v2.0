package com.example.ai_tutor.domain.practice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePracticeReq {

    // 문제 개수
    @Schema(type = "int", example ="10", description="생성할 문제의 개수입니다. 0일 경우 AI 추천(10개)")
    private int practiceSize;

    // 문제 유형
    @Schema(type = "String", example ="OX", description="생성할 문제의 유형입니다. OX(OX문제), MULTIPLE(객관식), BOTH(혼합)")
    private String type;

    @Schema(type = "String", example ="네트워크, 강의, OSI", description="문제 생성 시 강조할 키워드입니다.")
    private String keywords;

    // 요구사항
    @Schema(type = "String", example ="요약문은 5줄 이상 7줄 이하로 작성하고, 말투는 ~입니다. 로 해줘.", description="요약문 생성 시 요청할 요구사항입니다.")
    @Column(columnDefinition = "TEXT")
    private String requirement;
}
