package com.example.ai_tutor.domain.practice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PracticeRes {

    @Schema( type = "Long", example ="1", description="문제의 id입니다.")
    public Long practiceId;

    @Schema( type = "String", example ="데이터 분석 프로젝트를 시작할 때 가장 먼저 이해해야 할 부분은 무엇일까요?", description="문제의 내용입니다.")
    public String content;

    @Schema( type = "Integer", example ="1", description="문제의 번호(순서)입니다.")
    public Integer sequence;

}
