package com.example.ai_tutor.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PracticeSaveApiResponse", description = "문제 저장 응답")
public class PracticeSaveApiResponse {

    @Schema(description = "성공 여부", example = "true")
    private boolean check;

    @ArraySchema(schema = @Schema(implementation = ProfessorPracticeRes.class))
    private List<ProfessorPracticeRes> information;
}
