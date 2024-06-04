package com.example.ai_tutor.domain.tutor.application;

import com.example.ai_tutor.domain.chatgpt.application.GptService;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.tutor.domain.Tutor;
import com.example.ai_tutor.domain.tutor.domain.repository.TutorRepository;
import com.example.ai_tutor.domain.tutor.dto.request.QuestionReq;
import com.example.ai_tutor.domain.tutor.dto.response.AnswerRes;
import com.example.ai_tutor.domain.tutor.dto.response.TutorRes;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.example.ai_tutor.global.payload.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
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

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final TutorRepository tutorRepository;
    private final GptService gptService;

    // 최초 호출 시 원문 내용 주입
    public ResponseEntity<?> initializeChatting(UserPrincipal userPrincipal, Long noteId) {
        Optional<Note> noteOptional = noteRepository.findById(noteId);
        DefaultAssert.isTrue(noteOptional.isPresent(), "해당 노트가 존재하지 않습니다.");
        Note note = noteOptional.get();

        gptService.startConversation(noteId, note.getOriginalText());

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(Message.builder().message("원문이 주입되었습니다.").build())
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @Transactional
    public ResponseEntity<?> questionToTutor(UserPrincipal userPrincipal, Long noteId, QuestionReq questionReq) throws JsonProcessingException {
        Optional<User> userOptional = userRepository.findById(userPrincipal.getId());
        DefaultAssert.isTrue(userOptional.isPresent(), "해당 사용자가 존재하지 않습니다.");
        User user = userOptional.get();

        Optional<Note> noteOptional = noteRepository.findById(noteId);
        DefaultAssert.isTrue(noteOptional.isPresent(), "해당 노트가 존재하지 않습니다.");
        Note note = noteOptional.get();

        DefaultAssert.isTrue(Objects.equals(note.getUser().getUserId(), userPrincipal.getId()), "잘못된 접근입니다.");
        // 챗지피티 호출
        String answer = gptService.getAssistantMsg(noteId, questionReq.getQuestion());
        // 질문 저장
        // 답변 저장
        Tutor tutor = Tutor.builder()
                .question(questionReq.getQuestion())
                .answer(answer)   // 튜터의 답변 저장
                .folder(note.getFolder())
                .note(note)
                .user(user).build();
        tutorRepository.save(tutor);
        // response 생성
        AnswerRes answerRes = AnswerRes.builder()
                .answer(tutor.getAnswer())
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(answerRes)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

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
