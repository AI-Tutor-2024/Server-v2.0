package com.example.ai_tutor.domain.note.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema( type = "String", example ="빅데이터기술특론 2주차", description="교수자가 생성한 노트의 제목입니다.")
    private String title; //노트 제목
}
