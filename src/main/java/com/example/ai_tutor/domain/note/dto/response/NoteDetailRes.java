package com.example.ai_tutor.domain.note.dto.response;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.summary.domain.Summary;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
// NoteDetailRes.java
@Builder
@Data
public class NoteDetailRes {
    private Long noteId;
    private String title;
    private Summary summary;
    private LocalDateTime createdAt;
    private String sttText;
    private String code;

    public static NoteDetailRes from(Note note) {
        return NoteDetailRes.builder()
                .noteId(note.getNoteId())
                .title(note.getTitle())
                .summary(note.getSummary()) // null 가능성 있다면 조건 처리 가능
                .createdAt(note.getCreatedAt())
                .sttText(note.getSttText())
                .code(note.getCode())
                .build();
    }
}