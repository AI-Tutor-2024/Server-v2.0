package com.example.ai_tutor.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
public class HomeUserRes {

    @Schema( type = "Long", example ="1", description="사용자의 id입니다.")
    public Long userId;

    @Schema( type = "String", example ="김융소", description = "사용자의 이름입니다.")
    public String name;

    @Schema( type = "day", example ="01", description="사용자의 학습일수입니다. 2자리 수로 포맷됩니다.")
    public String day;

    @Builder
    public HomeUserRes(Long userId, String name, String day) {
        this.userId = userId;
        this.name = name;
        this.day = day;
    }

}
