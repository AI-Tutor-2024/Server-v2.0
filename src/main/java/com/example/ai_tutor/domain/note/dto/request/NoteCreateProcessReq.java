package com.example.ai_tutor.domain.note.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoteCreateProcessReq {
    //post요청으로 user_id(Long), folder_id(Long), note_id(Long), 음성 url(String) 보내기
    private Long userId;
    private Long folderId;
    private Long noteId;
    private String recordUrl;

}
