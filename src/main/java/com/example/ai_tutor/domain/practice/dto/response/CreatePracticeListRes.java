package com.example.ai_tutor.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePracticeListRes {

    @ArraySchema(schema = @Schema(implementation = CreatePracticeRes.class))
    private List<CreatePracticeRes> practiceResList;

    @Schema(type = "String", description = "강의의 요약본입니다.")
    @Column(columnDefinition = "TEXT")
    private String summary;   // 확인 및 수정 필요
}
