package com.example.ai_tutor.global.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
public class ApiResponse<T> {

    @Schema(type = "boolean", example = "true", description = "올바르게 로직을 처리했으면 true, 아니면 false를 반환합니다.")
    private boolean check;

    @Schema(description = "응답 데이터를 감싸는 객체입니다. 실제 응답 내용이 여기에 포함됩니다.")
    private T information;

    public ApiResponse() {}

    @Builder
    public ApiResponse(boolean check, T information) {
        this.check = check;
        this.information = information;
    }
}
