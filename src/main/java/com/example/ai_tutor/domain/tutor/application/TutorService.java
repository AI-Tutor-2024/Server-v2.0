package com.example.ai_tutor.domain.tutor.application;

import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.tutor.domain.Tutor;
import com.example.ai_tutor.domain.tutor.domain.repository.TutorRepository;
import com.example.ai_tutor.domain.tutor.dto.request.QuestionReq;
import com.example.ai_tutor.domain.tutor.dto.response.TutorRes;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final NoteRepository noteRepository;
    private final TutorRepository tutorRepository;

    // 챗봇
    //@Transactional
    //public ResponseEntity<?> questionToTutor(UserPrincipal userPrincipal, QuestionReq questionReq) {
        // 질문 저장
        // 프롬프트 답변 생성
        // 답변 저장
        // response
    //}

    // 기존 학습 질문 및 답변 조회
    public ResponseEntity<?> getQuestionAndAnswer(UserPrincipal userPrincipal, Long noteId) {
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        DefaultAssert.isTrue(noteOptional.isPresent(), "해당 노트가 존재하지 않습니다.");
        Note note = noteOptional.get();
        // 사용자 검증
        DefaultAssert.isTrue(Objects.equals(note.getUser().getUserId(), userPrincipal.getId()), "잘못된 접근입니다.");

        List<Tutor> tutors = tutorRepository.findByNoteOrderByCreatedAt(note);

        List<TutorRes> tutorResList = tutors.stream()
                .map(tutor -> TutorRes.builder()
                        .tutorId(tutor.getTutorId())
                        .question(tutor.getQuestion())
                        .answer(tutor.getAnswer())
                        .build())
                .toList();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(tutorResList)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
