package com.example.ai_tutor.domain.practice.application;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.chatgpt.application.GptService;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.practice.domain.PracticeType;
import com.example.ai_tutor.domain.practice.domain.repository.PracticeRepository;
import com.example.ai_tutor.domain.practice.dto.request.CreatePracticeReq;
import com.example.ai_tutor.domain.practice.dto.request.SavePracticeListReq;
import com.example.ai_tutor.domain.practice.dto.request.SavePracticeReq;
import com.example.ai_tutor.domain.practice.dto.request.UpdateLimitAndEndReq;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeListRes;
import com.example.ai_tutor.domain.practice.dto.response.CreatePracticeRes;
import com.example.ai_tutor.domain.practice.dto.response.ProfessorPracticeListRes;
import com.example.ai_tutor.domain.practice.dto.response.ProfessorPracticeRes;
import com.example.ai_tutor.domain.professor.domain.Professor;
import com.example.ai_tutor.domain.professor.domain.repository.ProfessorRepository;
import com.example.ai_tutor.domain.summary.application.SummaryService;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessorPracticeService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final ProfessorRepository professorRepository;
    private final PracticeRepository practiceRepository;

    private final GptService gptService;
    private final SummaryService summaryService;

    // 문제 생성
    public ResponseEntity<?> generatePractice(CreatePracticeReq createPracticeReq, MultipartFile file) throws IOException, JsonProcessingException {
        // User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//        User user = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 요약문 (파일 기반으로 생성)
        // TODO: createPractice.getRequestment()로 요구사항 입력
        String summary = summaryService.createSummary(file, createPracticeReq.getKeywords(), createPracticeReq.getRequirement());

        // 문제 개수 및 유형
        // TODO: createPractice.getKeywords()로 키워드 입력
        int practiceSize = createPracticeReq.getPracticeSize();

        // 문제 개수가 0이면 기본값 10으로 설정
        if (practiceSize <= 0) {practiceSize = 10;}

        List<CreatePracticeRes> practices = new ArrayList<>();

        if ("BOTH".equalsIgnoreCase(createPracticeReq.getType())) {
            // OX문제와 단답형 문제 반반 생성
            int oxCount = practiceSize % 2 == 0 ? practiceSize / 2 : practiceSize / 2 + 1;
            int shortCount = practiceSize - oxCount;

            List<CreatePracticeRes> oxPractices = gptService.generateQuestions(summary, oxCount, "OX", 1);
            List<CreatePracticeRes> shortPractices = gptService.generateQuestions(summary, shortCount, "SHORT", oxCount + 1);

            practices.addAll(oxPractices);
            practices.addAll(shortPractices);
        } else {
            // 지정된 문제 유형에 맞는 문제 생성
            practices = gptService.generateQuestions(summary, practiceSize, createPracticeReq.getType(), 1);
        }

        CreatePracticeListRes createPracticeListRes = CreatePracticeListRes.builder()
                .practiceResList(practices)
                .summary(summary)
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(createPracticeListRes)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    // 문제 저장
    @Transactional
    public ResponseEntity<?> savePractice(Long noteId, List<SavePracticeReq> savePracticeReqs) {
       // User user = userRepository.findById(userPrincipal.getId())
        //.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User user = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

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
    public ResponseEntity<?> getPractices(Long noteId) {
        //User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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
