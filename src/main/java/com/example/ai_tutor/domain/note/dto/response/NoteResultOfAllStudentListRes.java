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
public class NoteResultOfAllStudentListRes {
    @Schema( type = "List", description="학생들의 문제지 풀이 결과 리스트입니다.")
    private NoteResultOfAllStudentDetailRes noteResultOfAllStudentDetailRes;
}
