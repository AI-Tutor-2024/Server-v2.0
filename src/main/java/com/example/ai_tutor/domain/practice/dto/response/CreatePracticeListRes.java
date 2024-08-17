package com.example.ai_tutor.domain.practice.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
}
