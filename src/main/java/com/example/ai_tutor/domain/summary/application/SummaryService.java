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
    private static final int CHUNK_TOKEN_SIZE = 7000;    // 청크 단위 토큰 수


    public Mono<String> processSttAndSummary(MultipartFile file, String keywords, String requirement, Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("해당 노트를 찾을 수 없습니다."));

        return clovaService.processSpeechToText(file)  // Clova STT 처리
                .flatMap(response -> {
                    log.info("clova STT 응답: ", response);
                    // STT 결과에서 텍스트 추출
                    String fullText = response.path("text").asText();

                    // 프롬프트 생성
                    String prompt = generatePrompt(fullText, keywords, requirement, false);

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
    private String generatePrompt(String fullText, String keywords, String requirement, boolean isChunk) {
        StringBuilder promptBuilder = new StringBuilder();

        if (isChunk) {
            // 청크 요약용 프롬프트
            promptBuilder.append("""
            당신은 전문적인 강의 분석가입니다.
            아래 강의 내용을 간결하고 논리적으로 요약하십시오.
        
            ---
            ### 역할과 목표
            - 역할: 주어진 강의 청크를 분석하고, 핵심 개념과 주요 이론만 추출하는 전문가입니다.
            - 목표: 최종 요약의 재료로 사용될 일관성 있고 체계적인 부분 요약을 생성하는 것입니다.
        
            ---
            ### 작성 기준
            1. 핵심 개념과 이론만 요약하며 불필요한 설명과 반복은 반드시 제거합니다.
            2. 모든 문장은 간결하고 논리적이어야 하며, 전문적이고 학문적인 어투로 작성합니다. 각 문장은 '~이다.'로 끝맺습니다.
            3. 제공된 키워드와 요구사항은 반드시 반영하고 논리적으로 통합합니다.
            4. 결과는 마크다운 형식으로 작성하며, 번호나 리스트를 사용하여 시각적 구분을 명확히 합니다.
            5. 요약 결과는 반드시 500단어 이상 800단어 이하로 제한합니다.
        
            ---
            """);

        } else {
            // 최종 요약용 프롬프트
            promptBuilder.append(PromptForSummary);
        }

        // 키워드 추가
        if (keywords != null && !keywords.isEmpty()) {
            promptBuilder.append("\n- 키워드: ").append(keywords);
        }

        // 요구사항 추가
        if (requirement != null && !requirement.isEmpty()) {
            promptBuilder.append("\n- 추가 요구사항: ").append(requirement);
        }

        // 실제 텍스트 추가
        promptBuilder.append("\n\n아래 내용을 요약하세요:\n").append(fullText);

        return promptBuilder.toString();
    }


    // basePrompt를 멤버 변수로 선언하여 재사용 가능하게 설정
    private final String PromptForSummary = """
당신은 다양한 전공 분야의 강의를 쉽고 명료하게 전달하는 교양서적의 저자입니다.
학습자가 복잡한 개념을 명확히 이해하고, 논리적으로 정리된 지식을 통해 사고를 확장할 수 있도록 돕는 것이 당신의 역할입니다.

※ 주의사항  
- 반드시 제공된 강의 원문(STT 변환 텍스트)의 내용만 사용하여 작성합니다.  
- 외부 지식이나 상상, 창작은 절대 포함하지 않습니다.  
- 원문에 없는 정보는 작성하지 않습니다.

---

### 역할과 목표
- 역할: 전공과 관계없이 누구나 이해할 수 있도록 논리적이고 명확한 설명을 제공하는 비문학 저자입니다.
- 목표: 학습자가 해당 강의의 개념과 논지를 체계적으로 파악하고, 이후 학습이나 실제 문제 해결에 활용할 수 있도록 돕습니다.

---

### 작성 기준 및 스타일

1. **톤과 문체**
   - 객관적이고 신뢰할 수 있는 설명을 제공합니다.
   - 친절하지만 가벼운 농담이나 감성적인 표현은 사용하지 않습니다.
   - 종결어는 "~입니다." "~합니다."를 사용하여 명료하게 전달합니다.
   - 기술적, 전공 용어는 간단한 정의나 설명을 통해 독자가 자연스럽게 이해할 수 있도록 풀어줍니다.

2. **구성**
   - 주제별로 명확하게 구분하며, 목차 또는 소제목을 통해 논지를 체계적으로 전개합니다.
   - "서론 - 본론 - 결론" 구조를 기반으로 하되, 흐름은 원문의 전개에 따라 유연하게 구성합니다.
   - 서론에서는 강의의 문제의식과 목적을 명확히 설명합니다
   - 핵심 개념, 사례, 이론이 논리적으로 연결되도록 설명합니다.

3. **내용 전달 방식**
   - 추상적인 이론은 구체적인 사례나 원문에 포함된 설명을 중심으로 풀어서 설명합니다.
   - "왜 이 개념이 중요한가", "어디에 적용되는가"를 원문의 맥락에서 설명합니다.
   - 강의에서 강조된 부분은 원문의 표현을 충실히 반영하여 강조합니다.
   - 내용의 전달된 의도를 파악하여, 강의의 의도와 다르게 해석되면 절대 안됩니다. 

4. **형식**
   - 명확하고 간결한 문장으로 서술하며, 한 문장은 지나치게 길지 않게 유지합니다.
   - 중복 표현은 피하고, 동일한 개념을 반복 설명하지 않습니다.
   - 불필요한 감정적 어구나 과도한 수식은 사용하지 않습니다.
   
4. **검증**
   - 요약을 작성한 후, 이전의 요약본과 작성된 요약본을 다시 확인하여, 핵심 겨냄이 빠짐없이 포함되어있는지와 내용의 왜곡이 없는지 확인하고 보완하시오. 

---

### 예시 스타일 (비문학 교양서 예시)

- **도입부 예시**  
  "사도행전은 초기 기독교 공동체가 직면한 사회적, 문화적 도전을 설명하고 있으며, 이를 통해 오늘날 다문화 사회에 대한 통찰을 제공합니다."

- **개념 설명 예시**  
  "니체가 언급한 '르상티망'은 억눌린 감정이 왜곡되어 타인에 대한 적대감으로 표출되는 심리 상태를 의미합니다. 강의에서는 이를 현대 사회의 무차별 공격과 같은 현상과 연결지어 설명하고 있습니다."

---

### 최종 안내
아래는 요약해야 할 강의 원문입니다.  
- 반드시 해당 텍스트만 기반으로, 논리적이고 체계적인 비문학 스타일로 작성하십시오.  
- 외부 지식이나 창작은 절대 포함하지 않습니다.
""";


    public ResponseEntity<?> getSummary(Long noteId) {
//        validateUser(userPrincipal);
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

                                // 기존 summary가 있는지 확인하고
                                summaryRepository.findByNote(note)
                                        .ifPresent(existing -> {
                                            // 있으면 삭제
                                            summaryRepository.delete(existing);
                                            log.info("기존 summary 삭제 완료 (noteId: {})", note.getNoteId());
                                        });

                                // 새로운 summary 생성 및 저장
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
        log.info("Chunk 개수: {}", chunks.size());

        return Flux.fromIterable(chunks)
                .delayElements(Duration.ofMillis(500))  // 1초 간격으로 호출
                .flatMapSequential(chunk -> getGptResult(keywords, requirement, chunk, true))
                .collectList();

    }

    /**
     * 부분 요약을 합쳐 최종 요약을 생성합니다.
     */
    public Mono<String> summarizeFinal(List<String> summaries, String keywords, String requirement) {
        String mergedSummary = String.join("\n", summaries);
        return getGptResult(keywords, requirement, mergedSummary, false);
    }


    private Mono<String> getGptResult(String keywords, String requirement, String mergedSummary, boolean isChunk) {
        String prompt = generatePrompt(mergedSummary, keywords, requirement, isChunk);
        validatePromptLength(prompt);

        return gptService.callChatGpt(prompt)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
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
