package com.example.ai_tutor.domain.note.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoteResultOfAllStudentDetailRes {
    @Schema( type = "String", example ="60211659", description="학생의 학번입니다.")
    private String studentNumber;

    @Schema( type = "String", example ="조이", description="학생의 이름입니다.")
    private String studentName;

    @Schema( type = "int", example ="9", description="학생이 맞춘 문제 수입니다.")
    private int correctCount;

    @Schema( type = "int", example ="10", description="해당 문제지의 전체 문제 수입니다.")
    private int totalCount;

}
