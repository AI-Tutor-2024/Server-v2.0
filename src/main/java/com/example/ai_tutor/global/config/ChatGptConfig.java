package com.example.ai_tutor.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatGptConfig {
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";

    //TODO: 모델 수정
    public static final String MODEL = "gpt-4";
    public static final Integer MAX_TOKEN = 300;
    public static final Boolean STREAM = false;
    // public static final String ROLE = "user";
    public static final Double TEMPERATURE = 0.6;
    public static final Double TOP_P = 1.0;
    public static final String MEDIA_TYPE = "application/json; charset=UTF-8";

    //completions : 질답
    public static final String URL = "https://api.openai.com/v1/chat/completions";

    public static final String CONTENT = "너는 학생들의 학습을 도와주는 튜터야. 질문에 대한 답은 3~4줄로 하고 학습과 관계없는 질문에는 대답할 수 없어.";

}