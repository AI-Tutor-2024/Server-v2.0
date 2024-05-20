package com.example.ai_tutor.domain.practice.application;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.practice.domain.repository.PracticeRepository;
import com.example.ai_tutor.domain.practice.dto.AnswerReq;
import com.example.ai_tutor.domain.practice.dto.PracticeRes;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.example.ai_tutor.global.payload.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final PracticeRepository practiceRepository;
    private final NoteRepository noteRepository;

    // Description: 문제 풀기
    // 문제 생성(저장)
    // generateQuestion

    // 문제 조회(1개씩)
    public ResponseEntity<?> getQuestion(UserPrincipal userPrincipal, Long noteId, int number) {
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        DefaultAssert.isTrue(noteOptional.isPresent(), "해당 노트가 존재하지 않습니다.");
        // user 추가할지?
        Practice practice = practiceRepository.findByNoteAndSequence(noteOptional.get(), number);
        PracticeRes practiceRes = PracticeRes.builder()
                .practiceId(practice.getPracticeId())
                .content(practice.getContent())
                .sequence(practice.getSequence())
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

        practice.updateUserAnswer(answerReq.getUserAnswer());

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(Message.builder().message("답변이 저장되었습니다.").build())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    // Description: 학습 결과보기
    // 문제에 대한 ai 답변 생성
    // 문제 조회 및 내 답변 조회
    // 문제 수정

}
