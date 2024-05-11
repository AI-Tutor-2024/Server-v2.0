package com.example.ai_tutor.domain.Folder.dto.response;

import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FolderListRes {
    private String folderName;
    private String professor;
}
