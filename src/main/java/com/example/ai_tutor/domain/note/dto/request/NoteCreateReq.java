package com.example.ai_tutor.domain.note.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NoteCreateReq {
    private String title; //노트 제목
    private MultipartFile recordFile; //녹음 파일
}
