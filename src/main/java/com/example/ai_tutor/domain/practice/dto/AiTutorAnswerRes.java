package com.example.ai_tutor.domain.practice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AiTutorAnswerRes {

    public String aiTutor;

    @Builder
    public AiTutorAnswerRes(String aiTutor) {
        this.aiTutor = aiTutor;
    }
}
