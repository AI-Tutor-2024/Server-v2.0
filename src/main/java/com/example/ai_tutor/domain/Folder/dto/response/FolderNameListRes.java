package com.example.ai_tutor.domain.Folder.dto.response;

import lombok.*;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FolderNameListRes {
    private Long folderId;
    private String folderName;
}
