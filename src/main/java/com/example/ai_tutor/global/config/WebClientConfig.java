package com.example.ai_tutor.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${openapi.chatgpt.url}")
    private String gptUrl;

    @Value("${openapi.clova.url}")
    private String clovaUrl;

    @Bean
    public WebClient gptWebClient() {
        return createWebClient(gptUrl);
    }

    @Bean
    public WebClient clovaWebClient() {
        return createWebClient(clovaUrl);
    }

    private WebClient createWebClient(String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}

