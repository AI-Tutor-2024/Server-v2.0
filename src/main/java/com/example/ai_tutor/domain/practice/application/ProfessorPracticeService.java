package com.example.ai_tutor.domain.practice.application;

import com.example.ai_tutor.domain.folder.domain.Folder;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.practice.domain.PracticeType;
import com.example.ai_tutor.domain.practice.domain.repository.PracticeRepository;
import com.example.ai_tutor.domain.practice.dto.request.CreatePracticeReq;
import com.example.ai_tutor.domain.practice.dto.request.SavePracticeReq;
import com.example.ai_tutor.domain.practice.dto.request.UpdatePracticeReq;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeListRes;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeRes;
import com.example.ai_tutor.domain.practice.dto.response.ProfessorPracticeRes;
import com.example.ai_tutor.domain.professor.domain.Professor;
import com.example.ai_tutor.domain.professor.domain.repository.ProfessorRepository;
import com.example.ai_tutor.domain.summary.application.SummaryService;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.example.ai_tutor.global.payload.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessorPracticeService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final ProfessorRepository professorRepository;
    private final PracticeRepository practiceRepository;
    private final QuizGeneratorService quizGeneratorService;
    private final SummaryService summaryService;

    public Mono<Note> getNoteAsync(Long noteId) {
        return Mono.fromCallable(() -> getNote(noteId))
                .subscribeOn(Schedulers.boundedElastic());  // 블로킹 작업 분리
    }

    public Mono<ApiResponse<CreatePracticeListRes>> generatePractice(UserPrincipal userPrincipal, Long noteId, CreatePracticeReq createPracticeReq) {
        return getNoteAsync(noteId)
                .flatMap(note -> {
                    String summary = note.getSummary().getContent();
                    int practiceSize = determinePracticeSize(createPracticeReq.getPracticeSize());

                    if ("BOTH".equalsIgnoreCase(createPracticeReq.getType())) {
                        return generateBothTypesOfQuestions(summary, practiceSize);
                    } else {
                        return generateSingleTypeOfQuestions(summary, practiceSize, createPracticeReq.getType());
                    }
                })
                .onErrorResume(error -> {
                    log.error("문제 생성 중 오류 발생", error);
                    return Mono.just(ApiResponse.<CreatePracticeListRes>builder()
                            .check(false)
                            .information(null)
                            .build());
                });
    }

    private Mono<ApiResponse<CreatePracticeListRes>> generateBothTypesOfQuestions(String summary, int practiceSize) {
        int oxCount = calculateOxCount(practiceSize);
        int shortCount = practiceSize - oxCount;

        Mono<List<CreatePracticeRes>> oxPracticesMono = quizGeneratorService.generateQuestions(summary, oxCount, "OX", 1);
        Mono<List<CreatePracticeRes>> shortPracticesMono = quizGeneratorService.generateQuestions(summary, shortCount, "SHORT", oxCount + 1);

        return Mono.zip(oxPracticesMono, shortPracticesMono)
                .map(tuple -> buildApiResponse(combineQuestions(tuple.getT1(), tuple.getT2()), summary));
    }

    private Mono<ApiResponse<CreatePracticeListRes>> generateSingleTypeOfQuestions(String summary, int practiceSize, String type) {
        return quizGeneratorService.generateQuestions(summary, practiceSize, type, 1)
                .map(practices -> buildApiResponse(practices, summary));
    }

    private int determinePracticeSize(int requestedSize) {
        return requestedSize > 0 ? requestedSize : 10;
    }

    private int calculateOxCount(int practiceSize) {
        return (practiceSize % 2 == 0) ? practiceSize / 2 : practiceSize / 2 + 1;
    }

    private List<CreatePracticeRes> combineQuestions(List<CreatePracticeRes> oxList, List<CreatePracticeRes> shortList) {
        List<CreatePracticeRes> combined = new ArrayList<>(oxList);
        combined.addAll(shortList);
        return combined;
    }

    private ApiResponse<CreatePracticeListRes> buildApiResponse(List<CreatePracticeRes> practices, String summary) {
        CreatePracticeListRes createPracticeListRes = CreatePracticeListRes.builder()
                .practiceResList(practices)
                .summary(summary)
                .build();

        return ApiResponse.<CreatePracticeListRes>builder()
                .check(true)
                .information(createPracticeListRes)
                .build();
    }

    // 문제 저장 메서드
    @Transactional
    public ResponseEntity<?> savePractice(UserPrincipal userPrincipal, Long noteId, List<SavePracticeReq> savePracticeReqs) {
        validateUser(userPrincipal);
        validateProfessor(userPrincipal);
        Note note = validateNote(noteId);


        List<Practice> practices = savePracticeReqs.stream().map(request -> {
            PracticeType type = PracticeType.valueOf(request.getPracticeType());
            List<String> add = type == PracticeType.OX ? null : request.getAdditionalResults();
            return Practice.builder()
                    .note(note)
                    .sequence(request.getPracticeNumber())
                    .content(request.getContent())
                    .additionalResults(add)
                    .result(request.getResult())
                    .solution(request.getSolution())
                    .practiceType(type)
                    .build();
        }).toList();

        practiceRepository.saveAll(practices);   // 배치 저장

        ApiResponse<List<ProfessorPracticeRes>> api = ApiResponse.<List<ProfessorPracticeRes>>builder()
                .check(true)
                .information(practices.stream().map(this::toResponse).toList())
                .build();
        return ResponseEntity.ok(api);
    }

    @Transactional
    public ResponseEntity<?> updatePractice(UserPrincipal userPrincipal, Long noteId,
                                            Long practiceId, UpdatePracticeReq req) {
        validateUser(userPrincipal);
        validateProfessor(userPrincipal);
        Note note = validateNote(noteId);     // 노트 존재 확인

        Practice practice = practiceRepository.findById(practiceId)
                .orElseThrow(() -> new IllegalArgumentException("문제가 존재하지 않습니다."));

        // 노트–문제 매핑 검증
        if (!practice.getNote().getNoteId().equals(note.getNoteId())) {
            throw new IllegalArgumentException("noteId와 practiceId가 일치하지 않습니다.");
        }

        practice.update(req);
        practiceRepository.save(practice);
        ApiResponse<ProfessorPracticeRes> api = ApiResponse.<ProfessorPracticeRes>builder()
                .check(true)
                .information(toResponse(practice))
                .build();
        return ResponseEntity.ok(api);
    }

    public ResponseEntity<?> getPractices(UserPrincipal userPrincipal, Long noteId) {
        validateUser(userPrincipal);
        Note note = validateNote(noteId);
        List<Practice> practices = practiceRepository.findByNoteOrderBySequenceAsc(note);

        List<ProfessorPracticeRes> practiceResList = practices.stream()
                .map(practice -> ProfessorPracticeRes.builder()
                        .practiceId(practice.getPracticeId())
                        .practiceNumber(practice.getSequence())
                        .content(practice.getContent())
                        .additionalResults(practice.getPracticeType() == PracticeType.OX ? null : practice.getAdditionalResults())
                        .result(practice.getResult())
                        .solution(practice.getSolution())
                        .practiceType(practice.getPracticeType().toString())
                        .build())
                .toList();

        ApiResponse<List<ProfessorPracticeRes>> apiResponse = ApiResponse.<List<ProfessorPracticeRes>>builder()
                .check(true)
                .information(practiceResList)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    public int[] convertToMinutesAndSeconds(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);
        return new int[] { minutes, seconds };
    }

    private User validateUser(UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private Professor validateProfessor(UserPrincipal userPrincipal) {
        return professorRepository.findByUser(userRepository.findById(userPrincipal.getId())
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")))
                .orElseThrow(() -> new IllegalArgumentException("교수를 찾을 수 없습니다."));
    }

    private Note validateNote(Long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));
    }

    public Note getNote(Long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));
    }

    private User getUser(UserPrincipal userPrincipal) {
        String email = userPrincipal.getEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    @Transactional
    public Mono<ResponseEntity<ApiResponse<List<ProfessorPracticeRes>>>> generateAndSavePractice(
            UserPrincipal userPrincipal, Long noteId, CreatePracticeReq createPracticeReq) {

        // 유저와 교수 인증 (예외 발생 시 중단)
        validateUser(userPrincipal);
        validateProfessor(userPrincipal);

        // 비동기로 노트 조회 (블로킹 방지)
        return getNoteAsync(noteId)
            .flatMap(note -> {
                String summary = note.getSummary().getContent();
                int practiceSize = determinePracticeSize(createPracticeReq.getPracticeSize());

                Mono<List<CreatePracticeRes>> practiceMono;
                if ("BOTH".equalsIgnoreCase(createPracticeReq.getType())) {
                    int oxCount = calculateOxCount(practiceSize);
                    int shortCount = practiceSize - oxCount;

                    Mono<List<CreatePracticeRes>> oxMono = quizGeneratorService.generateQuestions(summary, oxCount, "OX", 1);
                    Mono<List<CreatePracticeRes>> shortMono = quizGeneratorService.generateQuestions(summary, shortCount, "SHORT", oxCount + 1);
                    practiceMono = Mono.zip(oxMono, shortMono)
                            .map(tuple -> combineQuestions(tuple.getT1(), tuple.getT2()));
                } else {
                    practiceMono = quizGeneratorService.generateQuestions(summary, practiceSize, createPracticeReq.getType(), 1);
                }

                // 기존 sequence 최대값 조회 → Mono로 감싸서 flatMap 안에서 사용
                return Mono.fromCallable(() -> practiceRepository.findMaxSequenceByNoteId(noteId))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(lastSequence -> practiceMono.flatMap(practices -> {

                        int startSequence = lastSequence + 1; // 기존 마지막 번호 다음부터 시작
                        List<Practice> entities = new ArrayList<>();

                        for (int i = 0; i < practices.size(); i++) {
                            CreatePracticeRes p = practices.get(i);
                            PracticeType type = PracticeType.valueOf(p.getPracticeType());

                            entities.add(Practice.builder()
                                .note(note)
                                .sequence(startSequence + i)
                                .content(p.getContent())
                                .result(p.getResult())
                                .solution(p.getSolution())
                                .additionalResults(type == PracticeType.OX ? null : p.getAdditionalResults())
                                .practiceType(type)
                                .build());
                        }

                        return Mono.fromCallable(() -> {
                            practiceRepository.saveAll(entities);
                            return entities;
                            }).subscribeOn(Schedulers.boundedElastic())
                            .map(saved -> {
                                List<ProfessorPracticeRes> responses = saved.stream()
                                    .map(this::toResponse)
                                    .toList();

                                ApiResponse<List<ProfessorPracticeRes>> api = ApiResponse.<List<ProfessorPracticeRes>>builder()
                                    .check(true)
                                    .information(responses)
                                    .build();

                                return ResponseEntity.ok(api);
                        });}));
            });

    }



    /** Practice → ProfessorPracticeRes 매핑용 */
    private ProfessorPracticeRes toResponse(Practice p) {
        return ProfessorPracticeRes.builder()
                .practiceId(p.getPracticeId())
                .practiceNumber(p.getSequence())
                .content(p.getContent())
                .additionalResults(p.getPracticeType() == PracticeType.OX ? null : p.getAdditionalResults())
                .result(p.getResult())
                .solution(p.getSolution())
                .practiceType(p.getPracticeType().toString())
                .build();
    }
}
