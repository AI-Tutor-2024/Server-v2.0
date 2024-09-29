package com.example.ai_tutor.domain.chatgpt.application;

import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeRes;
import com.example.ai_tutor.global.config.ChatGptConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GptService {

    @Value("${chatgpt.api-key}")
    private String apiKey;

    public List<CreatePracticeRes> generateQuestions(String summary, int size, String type, int num) throws JsonProcessingException {
        List<CreatePracticeRes> practiceResList;
        log.info(String.valueOf(size));
        String prompt = generatePrompt(summary, type, size);
        log.info("=============================================");
        log.info(prompt);
        // GPT API 호출
        JsonNode response = callChatGpt(prompt);
        log.info("=============================================");
        log.info(response.asText());
        // 응답 파싱
        practiceResList = parseResponse(response, num, type);
        return practiceResList;
    }


    // 프롬프트 생성
    private String generatePrompt(String summary, String type, int size) {
        switch (type.toUpperCase()) {
            case "OX":
                return String.format(
                        "다음 요약문을 바탕으로 OX 문제를 정확히 %d개 생성해주세요: %s\n" +
                                "당신은 문제집이며, 다음 형식을 엄격히 따라 문제와 해설을 제공해 주세요:\n\n" +
                                "각 문제는 반드시 '문제', '정답', '해설'로 구성되어야 하며, 각 부분은 누락 없이 제공되어야 합니다.\n" +
                                "OX 문제는 정확히 %d개를 생성하고, 문제 번호는 1부터 시작합니다.\n\n" +
                                "다음은 제공된 요약문을 통해 출력해야하는 OX 문제의 출력 형식입니다:\n\n" +
                                "문제 1: [문제 내용]\n" +
                                "정답: [O 또는 X]\n" +
                                "해설: [해설 내용]\n\n" +
                                "문제 2: [문제 내용]\n" +
                                "정답: [O 또는 X]\n" +
                                "해설: [해설 내용]\n\n" +
                                "... (계속)\n\n" +
                                "반드시 문제 형식과 문제 개수를 준수해 주세요. 정해진 형식을 따르지 않으면 응답을 처리할 수 없습니다.",
                        size, summary, size
                );

            case "SHORT":
                return String.format(
                        "다음 요약문을 바탕으로 단답형 문제를 %d개 생성해주세요: %s\n" +
                                "다음 형식으로 문제와 선택지, 정답, 해설을 포함하여 주세요: \n\n" +
                                "다음은 단답형 문제와 해설입니다. 각 문제에 대한 정답과 해설을 포함하여, 문제 번호와 함께 제공해 주세요. 출력 시 반드시 서두를 제외하고 문제와 정답, 해설만 포함시켜 주세요.\n\n" +
                                "문제 1: [문제 내용]\n" +
                                "정답: [정답]\n" +
                                "해설: [해설]\n\n" +
                                "문제 2: [문제 내용]\n" +
                                "정답: [정답]\n" +
                                "해설: [해설]\n\n" +
                                "... (계속)",
                        size, summary
                );

            default:
                throw new IllegalArgumentException("지원하지 않는 문제 유형입니다: " + type);
        }
    }



    // GPT API 호출
    private JsonNode callChatGpt(String prompt) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("model", ChatGptConfig.MODEL);
        bodyMap.put("max_tokens", ChatGptConfig.MAX_TOKEN);
        bodyMap.put("messages", List.of(
                Map.of(
                        "role", "user",
                        "content", prompt
                )
        ));

        String body = objectMapper.writeValueAsString(bodyMap);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        // 응답 상태 코드와 본문을 로그로 출력하여 디버깅
        try {
            ResponseEntity<String> response = restTemplate.exchange(ChatGptConfig.URL, HttpMethod.POST, request, String.class);

            // 응답 상태 코드 확인
            log.info("GPT API 응답 상태 코드: {}", response.getStatusCode());
            log.info("GPT API 응답 본문: {}", response.getBody());

            // 응답 본문을 JSON으로 변환하여 반환
            return objectMapper.readTree(response.getBody());

        } catch (Exception e) {
            log.error("GPT API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("GPT API 호출 실패", e);  // 예외를 던져 상위에서 처리
        }
    }

    private List<CreatePracticeRes> parseResponse(JsonNode response, int startPracticeNumber, String practiceType) {
        List<CreatePracticeRes> practiceResList = new ArrayList<>();

        // GPT 응답에서 질문, 답변, 해설 추출
        String textResponse = response.path("choices").get(0).path("message").path("content").asText().trim();
         log.info("Full Text Response: " + textResponse); // 전체 응답 확인

        // 문제 번호 및 내용 구분
        String[] problems = textResponse.split("(?=문제 \\d+:)");

         log.info("Number of Problems: " + problems.length); // 문제 개수 확인

        for (String problem : problems) {
            if (problem.trim().isEmpty()) continue;
            log.info("Problem Segment: {}", problem); // 문제 확인

            // 문제, 정답, 해설 파싱
            String problemPrefix = "문제 ";
            String answerPrefix = "정답:";
            String explanationPrefix = "해설:";

            int startIndexProblem = problem.indexOf(problemPrefix);
            int endIndexAnswer = problem.indexOf(answerPrefix);

//             인덱스 예외 확인
             if (startIndexProblem == -1 || endIndexAnswer == -1 || startIndexProblem >= endIndexAnswer) {
                 log.info("Missing Prefixes or Invalid Indices: Problem Index = " + startIndexProblem + ", Answer Index = " + endIndexAnswer); // 디버깅
                 continue;
             }

            startIndexProblem += problemPrefix.length();
            String content = problem.substring(startIndexProblem, endIndexAnswer).trim();

            // 문제 번호와 콜론 제거
            content = content.replaceAll("^\\d+: ", "").trim();

            int startIndexAnswer = endIndexAnswer + answerPrefix.length();
            int endIndexExplanation = problem.indexOf(explanationPrefix);

            if (endIndexExplanation == -1) {
                endIndexExplanation = problem.length();
            }

            String result = problem.substring(startIndexAnswer, endIndexExplanation).trim();
            String solution = problem.substring(endIndexExplanation + explanationPrefix.length()).trim();

            practiceResList.add(CreatePracticeRes.builder()
                    .practiceNumber(startPracticeNumber++)
                    .content(content)
                    .result(result)
                    .solution(solution)
                    .practiceType(practiceType)
                    .build());
        }
        return practiceResList;
    }
}