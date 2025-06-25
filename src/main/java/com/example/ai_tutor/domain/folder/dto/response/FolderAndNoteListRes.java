package com.example.ai_tutor.domain.folder.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolderAndNoteListRes {
    @Schema(type = "Long", example = "3", description = "폴더의 개수입니다.")
    private int folderCount;

    @Schema(type = "List", description = "폴더 목록입니다.")
    private List<FolderAndNoteDetailRes> folderNoteDetailList;

}


