package com.example.ai_tutor.domain.Folder.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
public class FolderCreateReq {
    private String folderName;
    private String professor;

    @Builder
    public FolderCreateReq(String folderName, String professor) {
        this.folderName = folderName;
        this.professor = professor;
    }

}
