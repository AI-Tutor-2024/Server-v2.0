package com.example.ai_tutor.domain.summary.application;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.openAPI.clova.ClovaService;
import com.example.ai_tutor.domain.openAPI.gpt.GptService;
import com.example.ai_tutor.domain.summary.domain.Summary;
import com.example.ai_tutor.domain.summary.domain.repository.SummaryRepository;
import com.example.ai_tutor.domain.summary.dto.response.SummaryRes;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

@Service
@RequiredArgsConstructor
@Slf4j
public class SummaryService {

    private final ClovaService clovaService;
    private final GptService gptService;
    private final SummaryRepository summaryRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    // gpt-4o 모델 기준 인코딩 설정
    private final Encoding encoding = Encodings.newDefaultEncodingRegistry()
            .getEncodingForModel("gpt-4o")
            .orElseThrow(() -> new IllegalArgumentException("Encoding 정보를 찾을 수 없습니다."));

    // 토큰 수 기준 제한값 (gpt-4o 기준으로 설정) 4o - 128000개의 토큰 지원
    private static final int MAX_PROMPT_TOKENS = 30000;  // 최종 요약 프롬프트 크기
    private static final int CHUNK_TOKEN_SIZE = 5000;    // 청크 단위 토큰 수


    public Mono<String> processSttAndSummary(MultipartFile file, String keywords, String requirement, Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("해당 노트를 찾을 수 없습니다."));

        return clovaService.processSpeechToText(file)  // Clova STT 처리
                .flatMap(response -> {
                    log.info("clova STT 응답: ", response);
                    // STT 결과에서 텍스트 추출
                    String fullText = response.path("text").asText();

                    // 프롬프트 생성
                    String prompt = generatePrompt(fullText, keywords, requirement);

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
     * @param fullText 원본 텍스트
     * @param keywords 요약 시 강조할 키워드 목록
     * @param requirement 요약 시 강조할 추가 요구사항
     * @return 요약을 위한 프롬프트 텍스트
     */
    private String generatePrompt(String fullText, String keywords, String requirement) {
        // 기본 프롬프트를 복사한 후, 동적으로 내용을 추가
        StringBuilder promptBuilder = new StringBuilder(PromptForSummary);

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
    private final String PromptForSummary = """
    당신은 다양한 전공 분야의 강의를 완벽하게 요약하는 전문 요약가입니다.
    아래 제공된 강의 원문을 바탕으로 논리적이고 핵심적인, 빠짐없는 요약문을 작성하십시오.
    
    ---
    ### 역할과 목표
    - 역할: 다양한 학과 전공(법학, 의학, 공학, 인문학 등)에 상관없이, 모든 학습자가 강의를 쉽고 정확하게 복습할 수 있도록 돕는 전문가입니다.
    - 목표: 학습자가 해당 강의의 모든 핵심 내용을 한눈에 이해하고, 이후 학습이나 시험 대비에 활용할 수 있도록 논리적이고 체계적인 요약문을 제공합니다.
    
    ---
    ### 작성 기준 및 규칙
    
    1. 구조화
       - 반드시 다음과 같은 목차 구조로 작성합니다.
         Ⅰ. 서론  
         Ⅱ. 주요 내용  
         Ⅲ. 결론 및 시사점  
       - 목차 순서(Ⅰ. → Ⅱ. → Ⅲ.)를 반드시 유지하십시오.
    
    2. 내용 구성
       - 각 목차 안에는 아래 내용을 반드시 포함합니다.  
         - 핵심 개념 및 정의  
         - 이론 및 원리 설명  
         - 강의에서 제시된 구체적 사례나 실습 내용  
         - 강사가 반복적으로 강조한 부분 및 주의점  
         - 과목 특성에 맞는 실용적 시사점 (예: 법학은 판례, 의학은 임상적용, 공학은 실무 적용 등)
       - 사례나 예시는 "누가, 언제, 무엇을, 어떻게, 왜"의 관점으로 구체적으로 설명하십시오.
    
    3. 표현 방식
       - 모든 문장은 간결하고 명확하게 작성하며, '~이다.'로 끝맺으십시오.
       - 동일한 개념이나 문장은 절대 반복하지 마십시오.
       - 항목별로 번호 또는 리스트를 사용해 시각적 구분을 명확히 하십시오.
    
    4. 불필요한 내용 제거
       - 강의 도중의 농담, 서론적인 발언, 잡담, 반복 내용 등 부수적인 정보는 절대 포함하지 않습니다.
    
    5. 키워드 반영 및 요구사항
       - 반드시 제공된 강조 키워드를 포함하고, 관련 개념과 논리적으로 연결하여 설명하십시오.
       - 추가 요구사항이 있는 경우, 이를 최우선으로 반영하십시오.
    
    6. 전체 검증
       - 요약을 작성한 후, 모든 핵심 개념이 빠짐없이 포함되었는지 다시 검토하고 누락된 내용이 있다면 보완하십시오.
       - 요약문 전체 분량은 최소 1500단어에서 최대 3000단어로 작성하십시오.
       - 단, 강의가 짧거나 요약 내용이 간결한 경우, 1000단어 이상으로 유지합니다.
            
    
    ---
    ### 예시 출력
    
    Ⅰ. 서론  
    - 본 강의는 "OOO 개념의 이해 및 실무 적용"을 목표로 한다.  
    - 학습자는 본 강의를 통해 OOO에 대한 기초 개념과 실무 응용 능력을 습득할 수 있다.
    
    Ⅱ. 주요 내용  
    1. OOO의 정의  
       - OOO이란 ...을 의미한다.  
    2. 핵심 이론 및 원리  
       - 첫째, ...이다.  
       - 둘째, ...이다.  
    3. 사례 및 실습  
       - 강사는 OOO 사례를 통해 이를 설명하였다.  
       - 예시: "2023년 법학 세미나에서 언급된 A 판결은 ...에 적용되었다."
    4. 주의점 및 강조 사항  
       - 강의에서는 특히 ...을 주의하라고 강조하였다.
    
    Ⅲ. 결론 및 시사점  
    - OOO 개념은 실제 ...에서 중요한 역할을 한다.  
    - 강사는 본 개념을 학습한 후, ... 분야에 적용할 것을 권장하였다.
    
    ---
    ### 최종 안내
    아래는 요약해야 할 강의 원문입니다.  
    위 작성 기준과 규칙을 충실히 반영하여 완전하고 체계적이며 논리적인 요약문을 작성하십시오.
""";


    public ResponseEntity<?> getSummary(UserPrincipal userPrincipal, Long noteId) {
        validateUser(userPrincipal);
        Note note = validateNote(noteId);

        // 요약 조회 로직 구현
        Summary summary = findSummaryByNote(note);
        SummaryRes sttRes = SummaryRes.builder()
                .summary(summary.getContent())
                .build();

        ApiResponse response = ApiResponse.builder()
                .check(true)
                .information(sttRes)
                .build();

        return ResponseEntity.ok(response);
    }

    private void validateUser(UserPrincipal userPrincipal){
        userRepository.existsById(userPrincipal.getId());
    }

    private Note validateNote(Long noteId){
        return noteRepository.findById(noteId).orElseThrow(()
                -> new IllegalArgumentException("노트를 찾을 수 없습니다."));
    }

    private Summary findSummaryByNote(Note note){
        return summaryRepository.findByNote(note)
                .orElseThrow(() -> new RuntimeException("해당 노트의 요약을 찾을 수 없습니다."));
    }



    // ===================================================================================================================

    /**
     * 저장된 STT 데이터를 사용하여 요약을 생성하고 결과를 반환합니다.
     */
    @Transactional
    public Mono<String> processSummaryFromSavedStt(Long noteId, String keywords, String requirement) {
        return Mono.fromCallable(() -> {
                    // 1. note 조회 및 fullText 추출 (블로킹)
                    Note note = noteRepository.findById(noteId)
                            .orElseThrow(() -> new RuntimeException("해당 노트를 찾을 수 없습니다."));

                    String fullText = note.getSttText();
                    if (fullText == null || fullText.isBlank()) {
                        throw new RuntimeException("STT 변환 데이터가 존재하지 않습니다.");
                    }

                    return Tuples.of(note, fullText);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tuple -> {
                    Note note = tuple.getT1();
                    String fullText = tuple.getT2();

                    List<String> chunks = splitTextIntoChunksByToken(fullText);

                    return summarizeChunks(chunks, keywords, requirement)
                            .flatMap(partialSummaries -> summarizeFinal(partialSummaries, keywords, requirement))
                            .flatMap(finalSummary -> Mono.fromCallable(() -> {
                                Summary summary = Summary.create(finalSummary, note);
                                summaryRepository.save(summary);
                                return finalSummary;
                            }).subscribeOn(Schedulers.boundedElastic()));
                })
                .onErrorResume(error -> {
                    log.error("요약 처리 중 오류 발생", error);
                    return Mono.error(new RuntimeException("요약 처리 실패", error));
                });
    }

    /**
     * 텍스트를 토큰 수 기준으로 나눕니다.
     */
    private List<String> splitTextIntoChunksByToken(String text) {
        List<String> chunks = new ArrayList<>();
        List<Integer> tokenIds = encoding.encode(text);
        int totalTokens = tokenIds.size();

        for (int start = 0; start < totalTokens; start += SummaryService.CHUNK_TOKEN_SIZE) {
            int end = Math.min(start + SummaryService.CHUNK_TOKEN_SIZE, totalTokens);
            List<Integer> chunkTokenIds = tokenIds.subList(start, end);
            String chunk = encoding.decode(chunkTokenIds);
            chunks.add(chunk);
        }
        return chunks;
    }

    /**
     * 각 청크에 대해 GPT를 호출하여 요약을 생성합니다.
     */
    public Mono<List<String>> summarizeChunks(List<String> chunks, String keywords, String requirement) {
        return Flux.fromIterable(chunks)
                .parallel(3) // 병렬 스트림 수 조정 가능
                .runOn(Schedulers.parallel())
                .flatMap(chunk -> getGptResult(keywords, requirement, chunk))
                .sequential()
                .collectList();
    }

    /**
     * 부분 요약을 합쳐 최종 요약을 생성합니다.
     */
    public Mono<String> summarizeFinal(List<String> summaries, String keywords, String requirement) {
        String mergedSummary = String.join("\n", summaries);
        return getGptResult(keywords, requirement, mergedSummary);
    }


    private Mono<String> getGptResult(String keywords, String requirement, String mergedSummary) {
        String prompt = generatePrompt(mergedSummary, keywords, requirement);
        validatePromptLength(prompt);

        return gptService.callChatGpt(prompt)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .doBeforeRetry(retrySignal -> log.warn("GPT 호출 재시도 {}회", retrySignal.totalRetriesInARow()))
                )
                .map(gptResponse -> {
                    JsonNode choices = gptResponse.get("choices");
                    return choices.get(0).get("message").get("content").asText();
                });
    }

    /**
     * 프롬프트 길이를 토큰 수 기준으로 검증합니다.
     */
    private void validatePromptLength(String prompt) {
        int tokenCount = encoding.countTokens(prompt);
        if (tokenCount > SummaryService.MAX_PROMPT_TOKENS) {
            throw new RuntimeException("프롬프트가 너무 깁니다. 토큰 수: " + tokenCount + "/" + SummaryService.MAX_PROMPT_TOKENS);
        }
    }


}
