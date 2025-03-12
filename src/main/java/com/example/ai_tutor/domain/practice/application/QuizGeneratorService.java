package com.example.ai_tutor.domain.practice.application;

import com.example.ai_tutor.domain.openAPI.gpt.GptService;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeRes;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizGeneratorService {

    private final GptService gptService;

    public Mono<List<CreatePracticeRes>> generateQuestions(String summary, int size, String type, int num) {
        // 프롬프트 생성
        String prompt = generatePrompt(summary, type, size);

        // 비동기 방식으로 GPT API 호출
        return gptService.callChatGpt(prompt)
                .flatMap(response -> {
                    if (response == null) {
                        return Mono.error(new RuntimeException("GPT API 응답이 없습니다."));
                    }

                    // 응답 파싱하여 List<CreatePracticeRes> 반환
                    List<CreatePracticeRes> practiceResList = parseResponse(response, num, type);
                    return Mono.just(practiceResList);
                })
                .doOnError(error -> log.error("문제 생성 중 오류 발생: ", error));
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
                                "다음은 제공된 요약문을 통해 출력해야 하는 OX 문제의 출력 형식입니다:\n\n" +
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

    // GPT 응답 파싱
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

            try {
                // 문제, 정답, 해설 파싱
                String content = extractSegment(problem, "문제 ", "정답:");
                String result = extractSegment(problem, "정답:", "해설:");
                String solution = extractSegment(problem, "해설:", null);

                practiceResList.add(CreatePracticeRes.builder()
                        .practiceNumber(startPracticeNumber++)
                        .content(content)
                        .result(result)
                        .solution(solution)
                        .practiceType(practiceType)
                        .build());
            } catch (Exception e) {
                log.error("문제 파싱 중 오류 발생: {}", e.getMessage());
            }
        }
        return practiceResList;
    }

    // 공통적인 세그먼트 추출 메서드
    private String extractSegment(String text, String startMarker, String endMarker) {
        int startIndex = text.indexOf(startMarker);
        if (startIndex == -1) {
            return ""; // 시작 마커가 없으면 빈 문자열 반환
        }

        startIndex += startMarker.length();

        int endIndex = (endMarker != null) ? text.indexOf(endMarker, startIndex) : text.length();
        if (endIndex == -1) {
            endIndex = text.length();
        }

        return text.substring(startIndex, endIndex).trim();
    }
}
