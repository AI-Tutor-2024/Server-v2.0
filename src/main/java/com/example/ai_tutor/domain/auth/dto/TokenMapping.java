package com.example.ai_tutor.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class TokenMapping {
    private String userEmail;
    private String accessToken;
    private String refreshToken;

    public TokenMapping(){}

    @Builder
    public TokenMapping(String userEmail, String accessToken, String refreshToken){
        this.userEmail = userEmail;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
