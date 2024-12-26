package com.example.ai_tutor.domain.summary.application;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.openAPI.application.ClovaService;
import com.example.ai_tutor.domain.openAPI.application.GptService;
import com.example.ai_tutor.domain.summary.domain.Summary;
import com.example.ai_tutor.domain.summary.domain.repository.SummaryRepository;
import com.example.ai_tutor.domain.summary.dto.response.SummaryRes;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private final ClovaService clovaService;
    private final GptService gptService;
    private final SummaryRepository summaryRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public Mono<String> processSttAndSummary(MultipartFile file, String keywords, String requirement, Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("해당 노트를 찾을 수 없습니다."));

        return clovaService.processSpeechToText(file)  // Clova STT 처리
                .flatMap(response -> {
                    // STT 결과에서 텍스트 추출
                    String fullText = response.path("text").asText();

                    // 프롬프트 생성
                    String prompt = generatePrompt(fullText, basePrompt1, keywords, requirement);

                    // GPT 요약 처리
                    return gptService.callChatGpt(prompt)
                            .<String>handle((gptResponse, sink) -> {
                                // GPT 응답에서 요약 추출
                                JsonNode choices = gptResponse.get("choices");
                                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                                    String summaryText = choices.get(0).get("message").get("content").asText();

                                    // Note 엔터티에 요약 저장
                                    Summary summary = Summary.builder()
                                            .content(summaryText)
                                            .note(note)
                                            .build();
                                    summaryRepository.save(summary);
                                    sink.next(summaryText);  // 요약 텍스트 반환
                                } else {
                                    sink.error(new RuntimeException("GPT 응답에서 선택지를 찾을 수 없습니다."));
                                }
                            });
                })
                .onErrorResume(error -> {
                    log.error("STT 및 요약 처리 중 오류 발생", error);
                    return Mono.error(new RuntimeException("STT 및 요약 처리 실패", error));
                });
    }

    /**
     * 프롬프트를 생성하는 메서드. 키워드 및 요구사항을 반영하여 텍스트를 요약하는 프롬프트를 동적으로 생성.
     *
     * @param basePrompt 기본 프롬프트 텍스트
     * @param fullText 원본 텍스트
     * @param keywords 요약 시 강조할 키워드 목록
     * @param requirement 요약 시 강조할 추가 요구사항
     * @return 요약을 위한 프롬프트 텍스트
     */
    private String generatePrompt(String fullText, String basePrompt, String keywords, String requirement) {
        // 기본 프롬프트를 복사한 후, 동적으로 내용을 추가
        StringBuilder promptBuilder = new StringBuilder(basePrompt);

        // 키워드가 있는 경우, 콤마로 구분된 키워드 추가
        if (keywords != null && !keywords.isEmpty()) {
            promptBuilder.append(" Additionally, here are the key keywords provided by the professor: ")
                    .append(keywords)
                    .append(". Please ensure these keywords are emphasized and integrated into the summary.");
        }

        // 요구사항이 있는 경우 추가
        if (requirement != null && !requirement.isEmpty()) {
            promptBuilder.append(" Special emphasis should be placed on the following points: ")
                    .append(requirement)
                    .append(". Make sure these aspects are addressed in detail.");
        }

        // 원본 텍스트 추가
        promptBuilder.append("\nSummarize the following fullText:\n").append(fullText);

        return promptBuilder.toString();
    }

    // basePrompt를 멤버 변수로 선언하여 재사용 가능하게 설정
    private final String basePrompt1 = """
        You are the professor giving this lecture.
        Your task is to summarize the key points of the lecture for your students.
        The goal is to create a clear, concise summary that captures the core ideas of the lecture and emphasizes the most important points.
        The summary should be between 8 to 15 sentences in length.
        
        Here’s how you will structure your response:
        Focus on the main arguments and key points that were repeated or emphasized during the lecture.
        Exclude irrelevant content such as greetings, jokes, or unrelated comments.
        Ensure that each sentence is short, direct, and focused on the topic.
        Keep the summary logically structured so that each idea flows from one to the next.
        
        The tone should be formal and objective, accurately reflecting the core content of the lecture.
        The final summary should be written in Korean, with each sentence ending in '다' or '이다.'
        
        As the professor, your role is to guide students in understanding the key points of your lecture,
        providing clarity and focus in the summary.
        """;

    public ResponseEntity<?> getSummary(UserPrincipal userPrincipal, Long noteId) {
//        User user = userRepository.findById(userPrincipal.getId())
//                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다."));

        User user = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("해당 노트를 찾을 수 없습니다."));

        // 요약 조회 로직 구현
        Summary summary = (Summary) summaryRepository.findByNote(note)
                .orElseThrow(() -> new RuntimeException("해당 노트의 요약을 찾을 수 없습니다."));
        SummaryRes sttRes = SummaryRes.builder()
                .summary(summary.getContent())
                .build();

        ApiResponse response = ApiResponse.builder()
                .check(true)
                .information(sttRes)
                .build();

        return ResponseEntity.ok(response);
    }
}
