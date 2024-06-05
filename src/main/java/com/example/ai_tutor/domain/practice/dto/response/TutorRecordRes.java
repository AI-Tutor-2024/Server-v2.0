package com.example.ai_tutor.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TutorRecordRes {

    @Schema( type = "String", example ="(추후 추가)", description="특정 답변에 대한 튜터의 tts 녹음파일 주소입니다.")
    private String tutorRecordUrl;
}
