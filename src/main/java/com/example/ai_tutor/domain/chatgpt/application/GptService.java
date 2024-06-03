package com.example.ai_tutor.domain.chatgpt.application;

import com.example.ai_tutor.domain.chatgpt.dto.ChatGptReq;
import com.example.ai_tutor.domain.chatgpt.dto.ChatGptRes;
import com.example.ai_tutor.global.config.ChatGptConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.flashvayne.chatgpt.service.ChatgptService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GptService {

    //private final ChatgptService chatgptService;

    //public String getGptResponse(String prompt) {
    //    return chatgptService.sendMessage(prompt);
    //}

    @Value("${chatgpt.api-key}")
    private String apiKey;

    public JsonNode callChatGpt(String userMsg) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", ChatGptConfig.MODEL);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userMsg);
        messages.add(userMessage);

        Map<String, String> assistantMessage = new HashMap<>();
        assistantMessage.put("role", "system");
        assistantMessage.put("content", ChatGptConfig.CONTENT);
        messages.add(assistantMessage);

        bodyMap.put("messages", messages);

        String body = objectMapper.writeValueAsString(bodyMap);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(ChatGptConfig.URL, HttpMethod.POST, request, String.class);

        return objectMapper.readTree(response.getBody());
    }

    public String getAssistantMsg(String userMsg) throws JsonProcessingException {
        JsonNode jsonNode = callChatGpt(userMsg);

        return jsonNode.path("choices").get(0).path("message").path("content").asText();
    }
}