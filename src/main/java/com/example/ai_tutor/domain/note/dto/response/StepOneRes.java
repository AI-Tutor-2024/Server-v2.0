package com.example.ai_tutor.domain.note.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StepOneRes {
    private int textId; //문단 번호이므로 int로 처리
    private String content;
    private Long summaryId;
    private String summary;
}
