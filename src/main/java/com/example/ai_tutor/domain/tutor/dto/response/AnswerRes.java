package com.example.ai_tutor.domain.tutor.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnswerRes {

    @Schema( type = "String", example ="계획적으로 공부하고 충분한 휴식을 취하세요.", description="질문에 대한 답변입니다.")
    public String answer;
}
