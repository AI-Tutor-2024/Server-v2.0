package com.example.ai_tutor.domain.Folder.dto.request;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FolderCreateReq {
    private String folderName;
    private String professor;
}
