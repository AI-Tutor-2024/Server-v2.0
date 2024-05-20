package com.example.ai_tutor.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class HomeUserRes {

    public Long userId;

    public String name;

    public String day;

    @Builder
    public HomeUserRes(Long userId, String name, String day) {
        this.userId = userId;
        this.name = name;
        this.day = day;
    }

}
