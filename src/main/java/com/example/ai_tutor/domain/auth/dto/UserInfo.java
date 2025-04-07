package com.example.ai_tutor.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfo {
    private String email;
    private String name;
}