package com.example.ai_tutor.domain.note.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoteAccessReq {
    @Schema( type = "String", example = "6DF1GH", description = "학생이 노트에 접속하기 위해 입력한 코드입니다.")
    private String code;
}
