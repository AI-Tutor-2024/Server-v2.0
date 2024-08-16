package com.example.ai_tutor.domain.note.application;

import com.amazonaws.services.s3.AmazonS3;
import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.Folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.answer.domain.repository.AnswerRepository;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.NoteStatus;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.note.dto.response.NoteListRes;
import com.example.ai_tutor.domain.note.dto.response.ProfessorNoteListDetailRes;
import com.example.ai_tutor.domain.note_student.domain.NoteStudent;
import com.example.ai_tutor.domain.note_student.domain.repository.NoteStudentRepository;
import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.practice.domain.repository.PracticeRepository;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessorNoteService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final PracticeRepository practiceRepository;
    private final AnswerRepository answerRepository;
    private final FolderRepository folderRepository;
    private final NoteStudentRepository noteStudentRepository;

    private final AmazonS3 amazonS3;
    private final WebClient webClient;

    // @Transactional
    // public ResponseEntity<?> createNewNote(UserPrincipal userPrincipal, Long folderId, NoteCreateReq noteCreateReq, MultipartFile recordFile) {
        // User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        // DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");

        // String fileName= UUID.randomUUID().toString();
        // try {
        //     amazonS3.putObject(new PutObjectRequest("ai-tutor-record", fileName, recordFile.getInputStream(), null));
        // } catch (IOException e) { throw new RuntimeException(e); }

        // String recordUrl = amazonS3.getUrl("ai-tutor-record", fileName).toString();
        // Note note = Note.builder()
        //         .title(noteCreateReq.getTitle())
                //.recordUrl(recordUrl)
        //         .step(0)
        //         .folder(folder)
                //.user(user)
        //         .build();

        // NoteCreateProcessReq noteCreateProcessReq = NoteCreateProcessReq.builder()
        //         .userId(user.getUserId())
        //         .folderId(folderId)
        //         .noteId(note.getNoteId())
        //         .recordUrl(recordUrl)
        //         .build();

        //post요청으로 user_id(Long), folder_id(Long), note_id(Long), 음성 url(String) 보내기
        // ResponseEntity requestResult = webClient.post()
        //         .uri("/start-process")
        //         .bodyValue(noteCreateProcessReq)
        //         .retrieve()
        //         .bodyToMono(ResponseEntity.class)
        //         .block();

        //상태코드로 완료 여부 판단
        // if(requestResult.getStatusCode().is2xxSuccessful()){
        //     noteRepository.save(note);
        //     ApiResponse apiResponse = ApiResponse.builder()
        //             .check(true)
        //             .information("노트 생성 성공")
        //             .build();

        //     return ResponseEntity.ok(apiResponse);
        // }
        // else{
        //     ApiResponse apiResponse = ApiResponse.builder()
        //             .check(false)
        //             .information("노트 생성 실패")
        //             .build();

        //     return ResponseEntity.badRequest().body(apiResponse);
        // }
    // }

    // 문제지 목록 조회
    public ResponseEntity<?> getAllNotesByFolder(UserPrincipal userPrincipal, Long folderId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        DefaultAssert.isTrue(user == folder.getProfessor().getUser(), "사용자가 일치하지 않습니다.");

        List<Note> notes = noteRepository.findAllByFolderOrderByCreatedAtDesc(folder);
        List<ProfessorNoteListDetailRes> noteListDetailRes = notes.stream()
                .map(note -> {
                    int studentSize = noteStudentRepository.countByNoteAndNoteStatus(note, NoteStatus.COMPLETED);
                    boolean isClosed = LocalDateTime.now().isAfter(note.getEndDate());
                    return ProfessorNoteListDetailRes.builder()
                            .noteId(note.getNoteId())
                            .title(note.getTitle())
                            .endDate(note.getEndDate())
                            .practiceSize(practiceRepository.countByNote(note))
                            .studentSize(studentSize)
                            .code(note.getCode())
                            .average(note.getAverage())
                            .closed(isClosed)
                            .build();
                })
                .toList();

        NoteListRes<ProfessorNoteListDetailRes> noteListRes = NoteListRes.<ProfessorNoteListDetailRes>builder()
                .folderName(folder.getFolderName())
                .professor(folder.getProfessor().getUser().getName())
                .noteListDetailRes(noteListDetailRes)
                .build();
        return ResponseEntity.ok(noteListRes);
    }

    // 문제지 삭제
    @Transactional
    public ResponseEntity<?> deleteNoteById(UserPrincipal userPrincipal, Long noteId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));

        Folder folder = note.getFolder();
        DefaultAssert.isTrue(folder.getProfessor().getUser() == user, "사용자가 일치하지 않습니다.");
        DefaultAssert.isTrue(note.getFolder().equals(folder), "해당 노트에 접근할 수 없습니다.");

        List<Practice> practiceList = practiceRepository.findByNote(note);
        // Practice 삭제 전에 Answer 확인
        // Answer가 있으면 응시한 사람이 있다는 뜻이므로 예외 처리
        practiceList.forEach(practice -> {
            boolean hasAnswer = answerRepository.existsByPractice(practice);
            DefaultAssert.isTrue(!hasAnswer, "이미 응시한 학생이 있습니다.");
            practiceRepository.delete(practice);
        });

        List<NoteStudent> noteStudentList = noteStudentRepository.findByNote(note);
        noteStudentList.forEach(noteStudent ->
                noteStudentRepository.delete(noteStudent));

        noteRepository.delete(note);
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("노트 삭제 성공")
                .build();

        return ResponseEntity.ok(apiResponse);

    }

}
