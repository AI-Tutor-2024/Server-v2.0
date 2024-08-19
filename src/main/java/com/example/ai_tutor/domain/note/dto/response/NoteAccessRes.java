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
public class NoteAccessRes {
    @Schema( type = "Long", example = "1", description = "노트의 id입니다.")
    private Long noteId;
}
