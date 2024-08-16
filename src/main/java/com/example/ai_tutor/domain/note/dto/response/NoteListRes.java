package com.example.ai_tutor.domain.note.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoteListRes<T> {

    @Schema( type = "String", example ="빅데이터기술특론", description="교수자가 생성한 폴더의 이름입니다.")
    private String folderName;

    @Schema( type = "String", example ="하석재", description="교수자의 이름입니다.")
    private String professor;

    @ArraySchema(schema = @Schema(type = "array", description = "노트 목록의 정보입니다. 학생 사용 시 StudentNoteListDetailRes, 교수자 사용 시 ProfessorNoteDetailRes를 확인해주세요.", implementation = Object.class))
    private List<T> noteListDetailRes;
}
