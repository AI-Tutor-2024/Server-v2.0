package com.example.ai_tutor.domain.Folder.dto.response;

import lombok.*;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FolderListRes {
    private Long folderId;
    private String folderName;
    private String professor;
}
