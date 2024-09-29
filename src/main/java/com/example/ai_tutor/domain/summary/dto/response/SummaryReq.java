package com.example.ai_tutor.domain.summary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SummaryReq {

    @Schema(type = "Array", example = "[\"keyword1\", \"keyword2\", \"keyword3\"]", description = "요약을 위한 키워드 목록입니다.")
    private List<String> keywords;

    private String requirements;
}
