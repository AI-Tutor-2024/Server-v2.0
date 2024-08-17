package com.example.ai_tutor.domain.chatgpt.application;

import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeRes;
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

    @Value("${chatgpt.api-key}")
    private String apiKey;

    public List<CreatePracticeRes> generateQuestions(String summary, int size, String type, int num) throws JsonProcessingException {
        List<CreatePracticeRes> practiceResList;
        String prompt = generatePrompt(summary, type, size);
        // GPT API 호출
        JsonNode response = callChatGpt(prompt);
        // 응답 파싱
        if (type.equals("OX")) {
            practiceResList = parseOxResponse(response, num);
        } else {
            practiceResList = parseMultipleChoiceResponse(response, num);
        }
        return practiceResList;
    }


    // 프롬프트 생성
    private String generatePrompt(String summary, String type, int size) {
        switch (type.toUpperCase()) {
            case "OX":
                return String.format(
                        "다음 요약문을 바탕으로 OX 문제를 %d개 생성해주세요: %s\n" +
                                "다음 형식으로 문제와 해설을 포함하여 주세요: \n\n" +
                                "다음은 OX 문제와 해설입니다. 각 문제에 대한 정답과 해설을 포함하여, 문제 번호와 함께 제공해 주세요. 출력 시 반드시 서두를 제외하고 문제와 정답, 해설만 포함시켜 주세요.\n\n" +
                                "문제 1: [문제 내용]\n" +
                                "정답: [정답]\n" +
                                "해설: [해설]\n\n" +
                                "문제 2: [문제 내용]\n" +
                                "정답: [정답]\n" +
                                "해설: [해설]\n\n" +
                                "... (계속)",
                        size, summary
                );

            case "MULTIPLE":
                return String.format(
                        "다음 요약문을 바탕으로 객관식 문제를 %d개 생성해주세요: %s\n" +
                                "다음 형식으로 문제와 선택지, 정답, 해설을 포함하여 주세요: \n\n" +
                                "다음은 객관식 문제와 해설입니다. 각 문제에 대한 네 가지 선택지, 정답, 해설을 포함하여, 문제 번호와 함께 제공해 주세요. 출력 시 반드시 서두를 제외하고 문제와 선택지, 정답, 해설만 포함시켜 주세요.\n\n" +
                                "문제 1: [문제 내용]\n" +
                                "A. [선택지 A]\n" +
                                "B. [선택지 B]\n" +
                                "C. [선택지 C]\n" +
                                "D. [선택지 D]\n" +
                                "정답: [정답]\n" +
                                "해설: [해설]\n\n" +
                                "문제 2: [문제 내용]\n" +
                                "A. [선택지 A]\n" +
                                "B. [선택지 B]\n" +
                                "C. [선택지 C]\n" +
                                "D. [선택지 D]\n" +
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
        ResponseEntity<String> response = restTemplate.exchange(ChatGptConfig.URL, HttpMethod.POST, request, String.class);

        return objectMapper.readTree(response.getBody());
    }

    private List<CreatePracticeRes> parseOxResponse(JsonNode response, int startPracticeNumber) {
        List<CreatePracticeRes> practiceResList = new ArrayList<>();

        // GPT 응답에서 질문, 답변, 해설 추출
        String textResponse = response.path("choices").get(0).path("message").path("content").asText().trim();
        //System.out.println("Full Text Response: " + textResponse); // 디버깅

        // 문제를 구분하는 정규 표현식 패턴 (문제 번호 및 내용 구분)
        String[] problems = textResponse.split("(?=문제 \\d+:)"); // 문제의 시작을 기준으로 분리

        System.out.println("Number of Problems: " + problems.length); // 디버깅

        for (String problem : problems) {
            if (problem.trim().isEmpty()) continue;
            System.out.println("Problem Segment: " + problem); // 디버깅

            // 문제, 정답, 해설 파싱
            String problemPrefix = "문제 ";
            String answerPrefix = "정답:";
            String explanationPrefix = "해설:";

            int startIndexProblem = problem.indexOf(problemPrefix);
            int endIndexAnswer = problem.indexOf(answerPrefix);

            if (startIndexProblem == -1 || endIndexAnswer == -1 || startIndexProblem >= endIndexAnswer) {
                System.out.println("Missing Prefixes or Invalid Indices: Problem Index = " + startIndexProblem + ", Answer Index = " + endIndexAnswer); // 디버깅
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
                    .choices(new ArrayList<>())  // OX 문제는 선택지가 없음
                    .result(result)
                    .solution(solution)
                    .practiceType("OX")
                    .build());
        }
        return practiceResList;
    }


    private List<CreatePracticeRes> parseMultipleChoiceResponse(JsonNode response, int startPracticeNumber) {
        List<CreatePracticeRes> practiceResList = new ArrayList<>();

        // GPT 응답에서 질문, 답변, 해설 추출
        String textResponse = response.path("choices").get(0).path("message").path("content").asText().trim();
        // System.out.println("Full Text Response: " + textResponse); // 디버깅

        // 문제를 구분하는 정규 표현식 패턴
        String[] problems = textResponse.split("(?=문제 \\d+:)");

        System.out.println("Number of Problems: " + problems.length); // 디버깅

        for (String problem : problems) {
            if (problem.trim().isEmpty()) continue;
            System.out.println("Problem Segment: " + problem); // 디버깅

            // 문제, 정답, 해설 파싱
            String problemPrefix = "문제 ";
            String answerPrefix = "정답:";
            String explanationPrefix = "해설:";

            int startIndexProblem = problem.indexOf(problemPrefix);
            int endIndexAnswer = problem.indexOf(answerPrefix);

            if (startIndexProblem == -1 || endIndexAnswer == -1 || startIndexProblem >= endIndexAnswer) {
                System.out.println("Missing Prefixes or Invalid Indices: Problem Index = " + startIndexProblem + ", Answer Index = " + endIndexAnswer); // 디버깅
                continue;
            }

            startIndexProblem += problemPrefix.length();
            String contentWithChoices = problem.substring(startIndexProblem, endIndexAnswer).trim();

            int startIndexAnswer = endIndexAnswer + answerPrefix.length();
            int endIndexExplanation = problem.indexOf(explanationPrefix);

            if (endIndexExplanation == -1) {
                endIndexExplanation = problem.length();
            }

            String result = problem.substring(startIndexAnswer, endIndexExplanation).trim();
            String solution = problem.substring(endIndexExplanation + explanationPrefix.length()).trim();

            // 디버깅 출력
            System.out.println("Parsed Content: " + contentWithChoices); // 디버깅
            System.out.println("Parsed Result: " + result); // 디버깅
            System.out.println("Parsed Solution: " + solution); // 디버깅

            // 객관식 선택지 추출
            List<String> choices = new ArrayList<>();
            String[] lines = contentWithChoices.split("\\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("A.") || line.startsWith("B.") || line.startsWith("C.") || line.startsWith("D.")) {
                    choices.add(line);
                }
            }

            // 문제 번호와 콜론 제거 및 문제 내용 추출
            String content = contentWithChoices.replaceAll("^(\\d+: )", "").trim();
            int endOfQuestionIndex = content.indexOf("\n");
            content = (endOfQuestionIndex == -1) ? content : content.substring(0, endOfQuestionIndex).trim();

            // CreatePracticeRes 객체 생성 및 리스트에 추가
            practiceResList.add(CreatePracticeRes.builder()
                    .practiceNumber(startPracticeNumber++)
                    .content(content)
                    .choices(choices)
                    .result(result)
                    .solution(solution)
                    .practiceType("MULTIPLE")
                    .build());
        }

        return practiceResList;
    }







}