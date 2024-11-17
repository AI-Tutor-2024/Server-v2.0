package com.example.ai_tutor.domain.practice.application;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.practice.domain.PracticeType;
import com.example.ai_tutor.domain.practice.domain.repository.PracticeRepository;
import com.example.ai_tutor.domain.practice.dto.request.CreatePracticeReq;
import com.example.ai_tutor.domain.practice.dto.request.SavePracticeReq;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeListRes;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeRes;
import com.example.ai_tutor.domain.practice.dto.response.ProfessorPracticeRes;
import com.example.ai_tutor.domain.professor.domain.Professor;
import com.example.ai_tutor.domain.professor.domain.repository.ProfessorRepository;
import com.example.ai_tutor.domain.summary.application.SummaryService;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

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

    // 문제 생성
    public Mono<ApiResponse> generatePractice(CreatePracticeReq createPracticeReq, MultipartFile file, Long noteId) {
        return summaryService.processSttAndSummary(file, createPracticeReq.getKeywords(), createPracticeReq.getRequirement(), noteId)
                .flatMap(summary -> {
                    int practiceSize = determinePracticeSize(createPracticeReq.getPracticeSize());

                    if ("BOTH".equalsIgnoreCase(createPracticeReq.getType())) {
                        return generateBothTypesOfQuestions(summary, practiceSize);
                    } else {
                        return generateSingleTypeOfQuestions(summary, practiceSize, createPracticeReq.getType());
                    }
                })
                .onErrorResume(error -> {
                    log.error("문제 생성 중 오류 발생", error);
                    return Mono.just(ApiResponse.builder()
                            .check(false)
                            .information("문제 생성 실패")
                            .build());
                });
    }


    // BOTH 타입의 문제 생성
    private Mono<ApiResponse> generateBothTypesOfQuestions(String summary, int practiceSize) {
        int oxCount = calculateOxCount(practiceSize);
        int shortCount = practiceSize - oxCount;

        Mono<List<CreatePracticeRes>> oxPracticesMono = quizGeneratorService.generateQuestions(summary, oxCount, "OX", 1);
        Mono<List<CreatePracticeRes>> shortPracticesMono = quizGeneratorService.generateQuestions(summary, shortCount, "SHORT", oxCount + 1);

        return Mono.zip(oxPracticesMono, shortPracticesMono)
                .map(tuple -> combineQuestionLists(tuple.getT1(), tuple.getT2(), summary));
    }

    // 단일 타입의 문제 생성
    private Mono<ApiResponse> generateSingleTypeOfQuestions(String summary, int practiceSize, String type) {
        return quizGeneratorService.generateQuestions(summary, practiceSize, type, 1)
                .map(practices -> buildApiResponse(practices, summary));
    }

    // 문제 생성 시 문제 수 설정
    // 기본 값 10으로 고정
    private int determinePracticeSize(int requestedSize) {
        return requestedSize > 0 ? requestedSize : 10;
    }

    // OX 문제 수 계산
    private int calculateOxCount(int practiceSize) {
        return (practiceSize % 2 == 0) ? practiceSize / 2 : practiceSize / 2 + 1;
    }

    // 문제 리스트 결합 (문제 타입이 BOTH인 경우 쓰임)
    private ApiResponse combineQuestionLists(List<CreatePracticeRes> oxQuestions, List<CreatePracticeRes> shortQuestions, String summary) {
        List<CreatePracticeRes> combinedList = new ArrayList<>();
        combinedList.addAll(oxQuestions);
        combinedList.addAll(shortQuestions);

        return buildApiResponse(combinedList, summary);
    }

    // 문제 생성 결과 반환
    private ApiResponse buildApiResponse(List<CreatePracticeRes> practices, String summary) {
        CreatePracticeListRes createPracticeListRes = CreatePracticeListRes.builder()
                .practiceResList(practices)
                .summary(summary)
                .build();

        return ApiResponse.builder()
                .check(true)
                .information(createPracticeListRes)
                .build();
    }



    // 문제 저장
    @Transactional
    public ResponseEntity<?> savePractice(UserPrincipal userPrincipal, Long noteId, List<SavePracticeReq> savePracticeReqs) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // User user = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Professor professor = professorRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("교수를 찾을 수 없습니다."));
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));

        // 제한시간을 밀리초로 변환
        // convertToMilliseconds(savePracticeListReq.getMinute(), savePracticeListReq.getSecond(), note);
        //note.updateEndDate(savePracticeListReq.getEndDate());

        for (SavePracticeReq req : savePracticeReqs) {
            List<String> additionalRes = Objects.equals(req.getPracticeType(), "OX") ? null : req.getAdditionalResults();
            Practice practice = Practice.builder()
                    .note(note)
                    .sequence(req.getPracticeNumber())
                    .content(req.getContent())
                    .additionalResults(additionalRes)
                    .result(req.getResult())
                    .solution(req.getSolution())
                    .practiceType(PracticeType.valueOf(req.getPracticeType()))
                    .build();
            practiceRepository.save(practice);
        }

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("문제가 저장되었습니다.")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // 문제 조회
    public ResponseEntity<?> getPractices(UserPrincipal userPrincipal, Long noteId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));
        List<Practice> practices = practiceRepository.findByNoteOrderBySequenceAsc(note);

        List<ProfessorPracticeRes> practiceResList = practices.stream()
                .map(practice -> {
                        List<String> additionalRes = practice.getPracticeType() == PracticeType.OX ? null : practice.getAdditionalResults();
                        return ProfessorPracticeRes.builder()
                                .praticeId(practice.getPracticeId())
                                .practiceNumber(practice.getSequence())
                                .content(practice.getContent())
                                .additionalResults(additionalRes)
                                .result(practice.getResult())
                                .solution(practice.getSolution())
                                .practiceType(practice.getPracticeType().toString())
                                .build();}
                        )
                        .toList();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(practiceResList)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    public int[] convertToMinutesAndSeconds(long milliseconds) {
        // 밀리초를 초로 변환
        long totalSeconds = milliseconds / 1000;
        // 초를 분과 초로 변환
        int minutes = (int) (totalSeconds / 60);
        int seconds = (int) (totalSeconds % 60);
        // 분과 초를 배열에 담아 반환
        return new int[] { minutes, seconds };
    }

}
