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

    @Schema( type = "String", example ="PROFESSOR", description="사용자의 역할입니다. ADMIN, STUDENT, PROFESSOR")
    public String role;

    @Builder
    public HomeUserRes(Long userId, String name, String role) {
        this.userId = userId;
        this.name = name;
        this.role = role;
    }

}
