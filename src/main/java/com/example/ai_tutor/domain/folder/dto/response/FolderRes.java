package com.example.ai_tutor.domain.folder.dto.response;

import com.example.ai_tutor.domain.folder.domain.Folder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolderRes {

    @Schema(type = "UUID", example = "1", description = "폴더의 UUID 값입니다.")
    private UUID folderUuid;

    @Schema(type = "int", example = "1", description = "폴더 내 노트 개수입니다.")
    private int noteCount;

    @Schema(type = "String", example = "빅데이터기술특론", description = "폴더의 이름입니다.")
    private String folderName;

    @Schema(type = "String", example = "하석재", description = "폴더를 생성한 교수자의 이름입니다.")
    private String professor;

    // Folder Entity -> FolderRes
    public static FolderRes fromEntity(Folder entity) {
        return FolderRes.builder()
                .folderUuid(entity.getFolderUuid())  // UUID 매핑
                .noteCount(entity.getNoteCount())    // 노트 개수 매핑
                .folderName(entity.getFolderName())  // 폴더 이름 매핑
                .build();
    }
}