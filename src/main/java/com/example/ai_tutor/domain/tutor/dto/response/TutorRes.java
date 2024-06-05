package com.example.ai_tutor.domain.tutor.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TutorRes {

    @Schema( type = "Long", example ="1", description="챗봇 질문/답변의 id입니다.")
    public Long tutorId;

    @Schema( type = "String", example ="기말고사 잘 보는 방법 알려줘", description="질문 내용입니다.")
    public String question;

    @Schema( type = "String", example ="계획적으로 공부하고 충분한 휴식을 취하세요.", description="질문에 대한 답변입니다.")
    public String answer;
}
