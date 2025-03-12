package com.example.ai_tutor.domain.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    ADMIN("ADMIN"),
    STUDENT("STUDENT"),
    PROFESSOR("PROFESSOR");

    private String value;
}