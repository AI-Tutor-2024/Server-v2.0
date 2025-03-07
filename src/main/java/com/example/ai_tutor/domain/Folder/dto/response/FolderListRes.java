package com.example.ai_tutor.domain.Folder.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FolderListRes {
    @Schema( type = "Long", example = "1", description = "폴더의 id입니다.")
    private Long folderId;
    @Schema( type = "String", example = "빅데이터기술특론", description = "폴더의 이름입니다.")
    private String folderName;
    @Schema( type = "String", example = "하석재", description = "폴더를 생성한 교수자의 이름입니다.")
    private String professor;
}
