package com.example.ai_tutor.domain.practice.application;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.practice.domain.repository.PracticeRepository;
import com.example.ai_tutor.domain.practice.dto.request.AnswerReq;
import com.example.ai_tutor.domain.practice.dto.request.UpdateAnswersReq;
import com.example.ai_tutor.domain.practice.dto.response.PracticeRes;
import com.example.ai_tutor.domain.practice.dto.response.PracticeResultsRes;
import com.example.ai_tutor.domain.practice.dto.response.TutorRecordRes;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.example.ai_tutor.global.payload.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final PracticeRepository practiceRepository;
    private final NoteRepository noteRepository;

    // Description: 문제 풀기

    // 문제 조회(1개씩)
    public ResponseEntity<?> getQuestion(UserPrincipal userPrincipal, Long noteId, int number) {
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        DefaultAssert.isTrue(noteOptional.isPresent(), "해당 노트가 존재하지 않습니다.");
        Note note = noteOptional.get();
        // 본인 노트 아니면 예외
        // DefaultAssert.isTrue(Objects.equals(note.getUser().getUserId(), userPrincipal.getId()), "사용자가 소유한 노트가 아닙니다.");
        // user 추가할지?
        List<Practice> practices = practiceRepository.findAllByNoteOrderByPracticeId(note);
        Practice practice = practices.get(number - 1);

        PracticeRes practiceRes = PracticeRes.builder()
                .practiceId(practice.getPracticeId())
                .content(practice.getContent())
                // number 맞춰서 문제 정렬 후 숫자에 맞는 값 가져오기
                .sequence(number)
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(practiceRes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // 사용자의 답변 작성(저장)
    @Transactional
    public ResponseEntity<?> registerAnswer(UserPrincipal userPrincipal, AnswerReq answerReq) {
        Optional<Practice> practiceOptional = practiceRepository.findById(answerReq.practiceId);
        DefaultAssert.isTrue(practiceOptional.isPresent(), "해당 문제가 존재하지 않습니다.");
        Practice practice = practiceOptional.get();

        // 사용자 검증
        // DefaultAssert.isTrue(Objects.equals(practice.getUser().getUserId(), userPrincipal.getId()), "잘못된 접근입니다.");

        // practice.updateUserAnswer(answerReq.getUserAnswer());

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(Message.builder().message("답변이 저장되었습니다.").build())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    // Description: 학습 결과보기

    // 문제 조회 및 내 답변, 튜터 답변 조회
    public ResponseEntity<?> getQuestionsAndAnswers(UserPrincipal userPrincipal, Long noteId) {
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        DefaultAssert.isTrue(noteOptional.isPresent(), "해당 노트가 존재하지 않습니다.");
        Note note = noteOptional.get();

        //DefaultAssert.isTrue(Objects.equals(note.getUser().getUserId(), userPrincipal.getId()), "잘못된 접근입니다.");

        List<Practice> practices = practiceRepository.findAllByNoteOrderByPracticeId(note);
        AtomicInteger sequence = new AtomicInteger(1);

        List<PracticeResultsRes> practiceResultsRes = practices.stream()
                .map(practice -> PracticeResultsRes.builder()
                        .practiceId(practice.getPracticeId())
                        .content(practice.getContent())
                        //.userAnswer(practice.getUserAnswer())
                        //.tutorAnswer(practice.getTutorAnswer())
                        .sequence(sequence.getAndIncrement()) // AtomicInteger로 sequence 값 증가
                        .build())
                .sorted(Comparator.comparing(PracticeResultsRes::getSequence))
                .collect(Collectors.toList());

        // test

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(practiceResultsRes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // 답변 수정
    @Transactional
    public ResponseEntity<?> updateMyAnswers(UserPrincipal userPrincipal, List<UpdateAnswersReq> updateAnswersReqs) {
        for (UpdateAnswersReq req : updateAnswersReqs) {
            Optional<Practice> practiceOptional = practiceRepository.findById(req.practiceId);
            DefaultAssert.isTrue(practiceOptional.isPresent(), "해당 문제가 존재하지 않습니다.");
            Practice practice = practiceOptional.get();

             // DefaultAssert.isTrue(Objects.equals(practice.getUser().getUserId(), userPrincipal.getId()), "사용자가 소유한 노트가 아닙니다.");
            // 새 답변을 입력한 경우만 업데이트
            if (req.getNewUserAnswer() != null) {
                //practice.updateUserAnswer(req.getNewUserAnswer());
            }
        }
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(Message.builder().message("답변이 수정되었습니다.").build())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    // tts 호출
    public ResponseEntity<?> getTutorRecord(UserPrincipal userPrincipal, Long practiceId) {
        Optional<Practice> practiceOptional = practiceRepository.findById(practiceId);
        DefaultAssert.isTrue(practiceOptional.isPresent(), "해당 문제가 존재하지 않습니다.");
        Practice practice = practiceOptional.get();

        //DefaultAssert.isTrue(Objects.equals(practice.getUser().getUserId(), userPrincipal.getId()), "사용자가 소유한 노트가 아닙니다.");

        TutorRecordRes tutorRecordRes = TutorRecordRes.builder()
                //.tutorRecordUrl(practice.getTutorRecordUrl())
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(tutorRecordRes)
                .build();

        return ResponseEntity.ok(apiResponse);

    }

}
