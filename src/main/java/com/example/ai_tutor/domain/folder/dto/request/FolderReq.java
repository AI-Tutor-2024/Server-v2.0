package com.example.ai_tutor.domain.folder.dto.request;

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
public class FolderReq {

    private UUID uuid;
    private String folderName;


    // 폴더 생성 요청
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FolderCreateReq {
        @Schema( type = "String", example ="빅데이터기술특론", description="폴더의 이름입니다.")
        private String folderName;
    }

    // 폴더 수정 요청
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FolderUpdateReq {
        @Schema( type = "String", example ="빅데이터기술특론", description="폴더의 이름입니다.")
        private String folderName;
    }

}
