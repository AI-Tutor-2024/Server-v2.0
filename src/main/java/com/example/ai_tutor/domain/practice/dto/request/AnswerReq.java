package com.example.ai_tutor.domain.practice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class AnswerReq {

    @Schema( type = "Long", example ="1", description="문제의 id입니다.")
    public Long practiceId;

    @Schema( type = "String", example ="의뢰기관에 대한 이해를 가장 먼저 해야 한다.", description="사용자의 답변입니다.")
    @Size(max = 500)
    public String userAnswer;
}
