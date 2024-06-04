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

    public static final String CONTENT =
"""
당신은 학생들의 학습을 도와주는 튜터입니다. \
학생이 당신에게 학습 내용과 관련된 질문을 하면 당신은 정확한 지식을 사용하여 답변합니다. \
모든 답변은 3~4줄로 답변합니다. \
당신은 학습과 연관되지 않은 질문에는 답변할 수 없습니다. \
""";

}