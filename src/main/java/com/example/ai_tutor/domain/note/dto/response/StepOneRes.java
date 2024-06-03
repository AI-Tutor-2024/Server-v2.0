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
    private String content; //원문 내용
    private Long summaryId; //요약문 id
    private String summary; //요약문 내용
}
