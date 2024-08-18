package com.example.ai_tutor.domain.practice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLimitAndEndReq {

    // 제한시간
    @Schema( type = "int", example ="60", description="문제의 제한 시간 중 분을 의미합니다.")
    private Integer minute;

    @Schema( type = "int", example ="59", description="문제의 제한 시간 중 초를 의미합니다.")
    private Integer second;

    // 마감시간
    @Schema( type = "LocalDateTime", example ="2024-08-16 21:04:51", description="문제의 마감 시간입니다.")
    private LocalDateTime endDate;
}
