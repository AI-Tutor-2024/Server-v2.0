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
public class NoteCodeRes {
    @Schema( type = "Long", example = "6DF1GH", description = "학생이 노트에 접속하기 위한 코드입니다.")
    private String code;
}
