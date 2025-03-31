package com.example.ai_tutor.global.config.security.auth;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuth2UserInfoFactory {
    public OAuth2UserInfo getOAuthUserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {

            case "google" -> new GoogleOAuth2UserInfo(attributes);

            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        };
    }
}
