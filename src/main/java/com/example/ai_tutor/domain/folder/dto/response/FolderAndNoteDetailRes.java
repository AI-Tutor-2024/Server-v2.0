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
public class FolderAndNoteDetailRes {

    @Schema(example = "1", description = "폴더의 id입니다.")
    private Long folderId;

    @Schema(example = "빅데이터기술특론", description = "폴더의 이름입니다.")
    private String folderName;


    @Schema(example = "2", description = "해당 폴더에 포함된 노트 개수입니다.")
    private int noteCount;

    @Schema(description = "노트 목록입니다.")
    private List<NotesInFolderRes> notesInFolderRes;

    public static FolderAndNoteDetailRes from(Long folderId, String folderName, List<NotesInFolderRes> notesInFolderRes) {
        return FolderAndNoteDetailRes.builder()
                .folderId(folderId)
                .folderName(folderName)
                .noteCount(notesInFolderRes != null ? notesInFolderRes.size() : 0)
                .notesInFolderRes(notesInFolderRes)
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotesInFolderRes {

        @Schema(example = "1", description = "노트의 id입니다.")
        private Long noteId;

        @Schema(example = "빅데이터기술특론 노트", description = "노트의 이름입니다.")
        private String noteName;

        public static NotesInFolderRes from(Long noteId, String noteName) {
            return NotesInFolderRes.builder()
                    .noteId(noteId)
                    .noteName(noteName)
                    .build();
        }
    }
}
