package com.example.ai_tutor.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessorPracticeListRes {

    @Schema(type = "Long", example = "1", description = "노트 ID입니다.")
    private Long noteId;

    @Schema(type = "String", example = "광복절 강의 노트", description = "노트 제목입니다.")
    private String noteTitle;

    @Schema(type = "Long", example = "2", description = "교수자의 ID입니다.")
    private Long professorId;

    @Schema(type = "String", example = "김철수", description = "교수자의 이름입니다.")
    private String professorName;

    @ArraySchema(schema = @Schema(implementation = ProfessorPracticeRes.class))
    private List<ProfessorPracticeRes> reqList;
}
