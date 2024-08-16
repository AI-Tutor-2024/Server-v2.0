package com.example.ai_tutor.domain.note.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema( type = "String", example ="빅데이터기술특론 1주차", description="교수자가 생성한 노트의 이름입니다.")
    private String title; //노트 제목

    @Schema( type = "int", example ="빅데이터기술특론 1주차", description="교수자가 생성한 폴더의 이름입니다.")
    private int step; //학습 단계 (1~4 , 4이면 완료인 상태)

    @Schema( type = "LocalDateTime", example ="2024-08-11 16:18:58.688107", description="학생의 응시일입니다.")
    private LocalDateTime createdAt;

    @Schema( type = "int", example ="10", description="문제의 개수입니다.")
    private int size;

    @Schema( type = "int", example ="90", description="사용자가 풀이한 문제의 총 점수입니다.")
    private int score;

}
