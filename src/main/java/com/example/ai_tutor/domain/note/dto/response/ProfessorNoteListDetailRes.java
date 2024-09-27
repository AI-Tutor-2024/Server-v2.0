package com.example.ai_tutor.domain.note.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ProfessorNoteListDetailRes {

    @Schema( type = "Long", example ="1", description="노트의 id입니다.")
    private Long noteId;

    @Schema( type = "String", example ="빅데이터기술특론 1주차", description="교수자가 생성한 노트의 이름입니다.")
    private String title; //노트 제목

    //@Schema( type = "String", example ="2024-08-11 16:18:58.688107", description="문제 풀이 기한입니다.")
    //private String endDate;

    @Schema( type = "int", example ="10", description="문제의 개수입니다.")
    private int practiceSize;

    //@Schema( type = "int", example ="90", description="응시한 학생의 수입니다.")
    //private int studentSize;

    //@Schema( type = "boolean", example ="true", description="해당 노트의 종료 여부입니다.")
    //private boolean closed;

    // 코드
    @Schema( type = "String", example ="9E79JE", description="노트의 코드입니다.")
    private String code;

    // 평균
    //@Schema( type = "double", example ="93.5", description="노트의 평균 점수입니다.")
    //private double average;

}
