package com.example.ai_tutor.domain.summary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SummaryRes {
    @Schema(description = "노트 번호")
    private Long noteId;
    @Schema(description = "요약된 텍스트")
    private String summary;
}
