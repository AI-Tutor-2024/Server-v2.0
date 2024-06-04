package com.example.ai_tutor.domain.chatgpt.application;

import com.example.ai_tutor.global.config.ChatGptConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GptService {

    private final Map<Long, List<Map<String, String>>> noteChatHistories = new HashMap<>();
    private final Map<Long, String> noteOriginalTexts = new HashMap<>();
    private final Map<Long, Integer> messageCounts = new HashMap<>(); // 메시지 카운트

    @Value("${chatgpt.api-key}")
    private String apiKey;

    // 대화 시작 시 원문을 포함시키는 메소드
    public void startConversation(Long noteId, String originalText) {
        noteOriginalTexts.put(noteId, originalText);
        clearChatHistory(noteId); // 대화 내역 초기화
        // 원문 설명 메시지 추가
        addOriginalTextToChatHistory(noteId);
    }

    private void addOriginalTextToChatHistory(Long noteId) {
        String originalText = noteOriginalTexts.get(noteId);
        if (originalText != null) {
            List<Map<String, String>> chatHistory = noteChatHistories.computeIfAbsent(noteId, k -> new ArrayList<>());
            Map<String, String> originalTextIntro = new HashMap<>();
            originalTextIntro.put("role", "system");
            originalTextIntro.put("content", "다음은 원문의 내용입니다:\n" + originalText);
            chatHistory.add(originalTextIntro);
        }
    }

    public JsonNode callChatGpt(Long noteId, String userMsg) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", ChatGptConfig.MODEL);
        bodyMap.put("max_tokens", ChatGptConfig.MAX_TOKEN);

        // 사용자 메시지와 컨텍스트 추가
        List<Map<String, String>> chatHistory = noteChatHistories.computeIfAbsent(noteId, k -> new ArrayList<>());
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", ChatGptConfig.CONTENT + userMsg);
        chatHistory.add(userMessage);

        bodyMap.put("messages", chatHistory);

        String body = objectMapper.writeValueAsString(bodyMap);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(ChatGptConfig.URL, HttpMethod.POST, request, String.class);

        JsonNode responseBody = objectMapper.readTree(response.getBody());

        // 대화 내역 업데이트
        Map<String, String> assistantResponse = new HashMap<>();
        assistantResponse.put("role", "assistant");
        assistantResponse.put("content", responseBody.path("choices").get(0).path("message").path("content").asText());
        chatHistory.add(assistantResponse);

        // 메세지 카운트 (15회마다 원문 주입)
        int count = messageCounts.getOrDefault(noteId, 0) + 1;
        messageCounts.put(noteId, count);

        if (count % 15 == 0) {
            addOriginalTextToChatHistory(noteId);
        }

        return responseBody;
    }

    public String getAssistantMsg(Long noteId, String userMsg) throws JsonProcessingException {
        JsonNode jsonNode = callChatGpt(noteId, userMsg);
        return jsonNode.path("choices").get(0).path("message").path("content").asText();
    }

    // 대화 내역 초기화
    public void clearChatHistory(Long noteId) {
        noteChatHistories.remove(noteId);
    }
}