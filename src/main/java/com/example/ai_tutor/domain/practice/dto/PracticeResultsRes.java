package com.example.ai_tutor.domain.practice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PracticeResultsRes {

    @Schema( type = "Long", example ="1", description="문제의 id입니다.")
    public Long practiceId;

    @Schema( type = "String", example ="데이터 분석 프로젝트를 시작할 때 가장 먼저 이해해야 할 부분은 무엇일까요?", description="문제의 내용입니다.")
    public String content;

    @Schema( type = "String", example ="의뢰기관에 대한 이해를 가장 먼저 해야 한다.", description="사용자의 답변입니다.")
    @Size(max = 500)
    public String userAnswer;

    @Schema( type = "String", example ="의뢰기관에 대한 이해를 가장 먼저 해야 한다.", description="튜터의 답변입니다.")
    public String tutorAnswer;

    @Schema( type = "Integer", example ="1", description="문제의 번호(순서)입니다.")
    public Integer sequence;
}
