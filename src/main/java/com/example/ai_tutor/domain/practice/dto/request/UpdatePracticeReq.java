package com.example.ai_tutor.domain.practice.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)   // null 필드는 직렬화하지 않음
public class UpdatePracticeReq {

    @Schema(type="int", example="2", description="(선택) 문제 번호")
    private Integer practiceNumber;

    @Schema(type="String", example="수정된 문제 내용", description="(선택) 문제 내용")
    private String content;

    @ArraySchema(schema=@Schema(type="String", example="사용 안하고 있어요. 추가 인정 답안이라고 합니다.", description="(선택) 추가 인정 답안"))
    private List<String> additionalResults;

    @Schema(type="String", example="O", description="(선택) 정답")
    private String result;

    @Schema(type="String", example="수정된 해설", description="(선택) 해설")
    private String solution;

    @Schema(type="String", example="SHORT", description="(선택) 문제 타입(OX 또는 SHORT)")
    private String practiceType;
}

