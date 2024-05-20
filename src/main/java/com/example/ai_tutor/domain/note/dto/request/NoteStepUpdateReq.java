package com.example.ai_tutor.domain.note.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoteStepUpdateReq {
    // folderId, step
    private Long folderId;
    private int step;

}
