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
public class FolderInfoRes {

    @Schema( type = "String", example ="빅데이터기술특론", description="교수자가 생성한 폴더의 이름입니다.")
    private String folderName;

    @Schema( type = "String", example ="하석재", description="교수자의 이름입니다.")
    private String professor;
}
