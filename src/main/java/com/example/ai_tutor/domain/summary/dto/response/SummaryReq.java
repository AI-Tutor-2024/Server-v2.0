package com.example.ai_tutor.domain.summary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SummaryReq {

    @Schema(type = "String", example = "원하는 중요 키워드를 입력하세요.", description = "키워드 문자열입니다.")
    private String keywords;

    @Schema(type = "String", example = "중요한 점들을 강조해주세요.", description = "요약 시 강조할 요구사항입니다.")
    private String requirement;
}

