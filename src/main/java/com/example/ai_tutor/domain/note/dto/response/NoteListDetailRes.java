package com.example.ai_tutor.domain.note.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoteListDetailRes {
    private String title; //노트 제목
    private int step; //학습 단계 (1~4 , 4이면 완료인 상태)
    private LocalDateTime createdAt;
    private int length;

}
