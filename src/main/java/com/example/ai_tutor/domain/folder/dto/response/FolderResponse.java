package com.example.ai_tutor.domain.folder.dto.response;

import com.example.ai_tutor.domain.folder.domain.Folder;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FolderResponse {
    private Long id;
    private String folderName;
    private String professorName;

    public static FolderResponse from(Folder folder) {
        return FolderResponse.builder()
                .id(folder.getFolderId())
                .folderName(folder.getFolderName())
                .professorName(folder.getProfessorName()) // or folder.getProfessor().getProfessorName()
                .build();
    }
}
