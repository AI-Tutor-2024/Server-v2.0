package com.example.ai_tutor.domain.auth.application;

import com.example.ai_tutor.domain.auth.dto.UserInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdTokenVerifier {

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${app.auth.token-secret}")
    private String jwtSecret;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public UserInfo verifyIdToken(String idToken, String email) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + idToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.postForEntity(googleUserInfoUri, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode userInfo = objectMapper.readTree(response.getBody());

                String tokenEmail = userInfo.path("email").asText();
                String name = userInfo.path("name").asText(); // 여기!

                if (!email.equals(tokenEmail)) {
                    throw new IllegalArgumentException("ID 토큰의 이메일과 요청된 이메일이 일치하지 않습니다.");
                }

                return new UserInfo(tokenEmail, name); // DTO에 담아 반환
            } else {
                throw new RuntimeException("ID 토큰 검증 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("ID 토큰 검증 중 오류 발생", e);
        }
    }


}