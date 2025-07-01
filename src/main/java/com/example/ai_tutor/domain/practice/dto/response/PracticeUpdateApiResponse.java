package com.example.ai_tutor.domain.practice.dto.response;

import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.practice.domain.PracticeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PracticeUpdateApiResponse", description = "문제 수정 응답")
public class PracticeUpdateApiResponse {

    @Schema(description = "성공 여부", example = "true")
    private boolean check;

    @Schema(description = "노트 ID", example = "1")
    private Long noteId;

    @Schema(description = "노트 제목", example = "광복절 강의 노트")
    private String noteTitle;

    @Schema(description = "교수 ID", example = "2")
    private Long professorId;

    @Schema(description = "교수 이름", example = "김철수")
    private String professorName;

    @Schema(description = "수정된 문제 정보")
    private ProfessorPracticeRes information;

    private ProfessorPracticeRes toResponse(Practice practice) {
        return ProfessorPracticeRes.builder()
            .practiceId(practice.getPracticeId())
            .practiceNumber(practice.getSequence())
            .content(practice.getContent())
            .additionalResults(practice.getPracticeType() == PracticeType.OX ? null : practice.getAdditionalResults())
            .result(practice.getResult())
            .solution(practice.getSolution())
            .practiceType(practice.getPracticeType().toString())
            .build();
    }

}
