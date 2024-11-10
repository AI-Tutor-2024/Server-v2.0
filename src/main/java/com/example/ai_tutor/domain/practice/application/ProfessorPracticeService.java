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
import org.springframework.http.HttpStatus;
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
                    // 문제 개수 및 유형 설정
                    int practiceSize = createPracticeReq.getPracticeSize() > 0 ? createPracticeReq.getPracticeSize() : 10;

                    if ("BOTH".equalsIgnoreCase(createPracticeReq.getType())) {
                        // OX문제와 단답형 문제 반반 생성
                        int oxCount = (practiceSize % 2 == 0) ? practiceSize / 2 : practiceSize / 2 + 1;
                        int shortCount = practiceSize - oxCount;

                        // OX 문제와 단답형 문제를 비동기적으로 병렬 처리
                        Mono<List<CreatePracticeRes>> oxPracticesMono = quizGeneratorService.generateQuestions(summary, oxCount, "OX", 1);
                        Mono<List<CreatePracticeRes>> shortPracticesMono = quizGeneratorService.generateQuestions(summary, shortCount, "SHORT", oxCount + 1);

                        // zip을 이용해 두 결과를 병렬 처리하고 결합
                        return Mono.zip(oxPracticesMono, shortPracticesMono)
                                .map(tuple -> {
                                    List<CreatePracticeRes> combinedList = new ArrayList<>();
                                    combinedList.addAll(tuple.getT1()); // OX 문제
                                    combinedList.addAll(tuple.getT2()); // 단답형 문제
                                    return combinedList;
                                })
                                .map(practices -> {
                                    CreatePracticeListRes createPracticeListRes = CreatePracticeListRes.builder()
                                            .practiceResList(practices)
                                            .summary(summary)
                                            .build();

                                    return ApiResponse.builder()
                                            .check(true)
                                            .information(createPracticeListRes)
                                            .build();
                                });
                    } else {
                        // 지정된 문제 유형에 맞는 문제 생성 (OX 또는 SHORT 중 하나)
                        return quizGeneratorService.generateQuestions(summary, practiceSize, createPracticeReq.getType(), 1)
                                .map(practices -> {
                                    CreatePracticeListRes createPracticeListRes = CreatePracticeListRes.builder()
                                            .practiceResList(practices)
                                            .summary(summary)
                                            .build();

                                    return ApiResponse.builder()
                                            .check(true)
                                            .information(createPracticeListRes)
                                            .build();
                                });
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

    //private void convertToMilliseconds(Integer minute, Integer second, Note note) {
    //    int totalMilliseconds = (minute * 60 * 1000) + (second * 1000);
    //    note.updateLimitTime(totalMilliseconds);
    //}

    // 제한시간 및 마감 시간 수정
//    @Transactional
//    public ResponseEntity<?> updateLimitTimeAndEndDate(Long noteId, UpdateLimitAndEndReq updateLimitAndEndReq) {
        //User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//        Note note = noteRepository.findById(noteId)
//                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));

        // Validation
//        if (updateLimitAndEndReq.getMinute() != null && updateLimitAndEndReq.getSecond() != null) {
//            if (updateLimitAndEndReq.getMinute() < 0 || updateLimitAndEndReq.getSecond() < 0) {
//                throw new IllegalArgumentException("시간과 초는 음수일 수 없습니다.");
//            }
//        }
//        // 마감기간 update
//        if (updateLimitAndEndReq.getEndDate() != null) {
//            note.updateEndDate(updateLimitAndEndReq.getEndDate());
//        }
        // 제한시간 update
//        if (updateLimitAndEndReq.getMinute() != null && updateLimitAndEndReq.getSecond() != null) {
//            convertToMilliseconds(updateLimitAndEndReq.getMinute(), updateLimitAndEndReq.getSecond(), note);
//        }
//
//        ApiResponse apiResponse = ApiResponse.builder()
//                .check(true)
//                .information("마감 기간, 제한 시간이 변경되었습니다.")
//                .build();

//        return ResponseEntity.ok(apiResponse);
//    }

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

        //int[] result = convertToMinutesAndSeconds(note.getLimitTime());

        //int minutes = result[0];
        //int seconds = result[1];

        //ProfessorPracticeListRes professorPracticeListRes = ProfessorPracticeListRes.builder()
        //        .minute(minutes)
        //        .second(seconds)
        //        .endDate(note.getEndDate())
        //        .reqList(practiceResList)
        //        .build();

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
