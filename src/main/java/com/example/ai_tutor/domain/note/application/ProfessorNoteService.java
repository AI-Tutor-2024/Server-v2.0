package com.example.ai_tutor.domain.note.application;

import com.amazonaws.services.s3.AmazonS3;
import com.example.ai_tutor.domain.answer.domain.repository.AnswerRepository;
import com.example.ai_tutor.domain.folder.domain.Folder;
import com.example.ai_tutor.domain.folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.NoteStatus;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateReq;
import com.example.ai_tutor.domain.note.dto.response.*;
import com.example.ai_tutor.domain.note_student.application.NoteStudentService;
import com.example.ai_tutor.domain.note_student.domain.NoteStudent;
import com.example.ai_tutor.domain.note_student.domain.repository.NoteStudentRepository;
import com.example.ai_tutor.domain.openAPI.clova.ClovaService;
import com.example.ai_tutor.domain.practice.domain.Practice;
import com.example.ai_tutor.domain.practice.domain.repository.PracticeRepository;
import com.example.ai_tutor.domain.professor.domain.Professor;
import com.example.ai_tutor.domain.professor.domain.repository.ProfessorRepository;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.example.ai_tutor.global.util.ConvertToMp3;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Random;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfessorNoteService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final PracticeRepository practiceRepository;
    private final AnswerRepository answerRepository;
    private final FolderRepository folderRepository;
    private final NoteStudentRepository noteStudentRepository;
    private final ProfessorRepository professorRepository;
    private final NoteStudentService noteStudentService;
    private final ClovaService clovaService;

    private final AmazonS3 amazonS3;
    private final ConvertToMp3 convertToMp3;

    // 노트 생성
    @Transactional
    public ResponseEntity<?> createNewNote(UserPrincipal userPrincipal, Long folderId, NoteCreateReq request) {
        User user = getUser(userPrincipal);
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        Professor professor = professorRepository.findByUser(user).orElseThrow(() -> new IllegalArgumentException("교수자를 찾을 수 없습니다."));

        DefaultAssert.isTrue(folder.getProfessor().equals(professor), "해당 폴더에 접근할 수 없습니다.");
        Note note = Note.createNote(folder, request.getTitle(), null, null);
        noteRepository.save(note);

        NoteAccessRes noteAccessRes = NoteAccessRes.builder()
                .noteId(note.getNoteId())
                .build();

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .check(true)
                .information(noteAccessRes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @Transactional
    public boolean convertSpeechToText(Long noteId, MultipartFile file) {
        // 1. Note 조회
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));

        // 2. 기존 STT 변환이 있다면 중단
        if (note.getSttText() != null) {
            log.info("기존 STT 변환 데이터가 존재하므로 변환하지 않습니다.");
            return false;
        }

        try {
            // 3. CLOVA STT API 호출 (동기 처리)
            log.info("CLOVA STT 응답");
            File mp3File = convertToMp3.convert(file);
            JsonNode response = clovaService.processSpeechToText(mp3File).block();
            log.info("CLOVA STT 응답: {}", response);

            // 4. STT 결과에서 텍스트 추출 및 저장
            String fullText = response.path("text").asText();
            note.updateStt(fullText, String.valueOf(response));
            noteRepository.save(note);

            log.info("STT 변환 완료: {}", noteId);
            return true;
        } catch (Exception e) {
            log.error("STT 변환 중 오류 발생", e);
            note.markSttFailed(); // 실패 상태 업데이트
            noteRepository.save(note);
            return false;
        }
    }

//
//    // 녹음본이 아닌 영상을 업로드하는 방식으로 수정
//     @Transactional
//     public ResponseEntity<?> createNewNote(   Long folderId, NoteCreateReq noteCreateReq, MultipartFile file) {
//         User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//
//         Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
//         DefaultAssert.isTrue(user == folder.getProfessor().getUser(), "사용자가 일치하지 않습니다.");
//
//         String fileName= UUID.randomUUID().toString();
//         try {
//             // S3에 파일 업로드
//             amazonS3.putObject(new PutObjectRequest("ai-tutor-record", fileName, file.getInputStream(), null));
//         } catch (IOException e) { throw new RuntimeException(e); }
//
////         String recordUrl = amazonS3.getUrl("ai-tutor-record", fileName).toString();
////         Note note = Note.builder()
////                 .title(noteCreateReq.getTitle())
////                 .
////                 .step(0)
////                 .folder(folder)
////                .user(user)
////                 .build();
//         // 요약문 추출
////         NoteCreateProcessReq noteCreateProcessReq = NoteCreateProcessReq.builder()
////                 .userId(user.getUserId())
////                 .folderId(folderId)
////                 .noteId(note.getNoteId())
////                 .recordUrl(recordUrl)
////                 .build();
//
//         // post요청으로 user_id(Long), folder_id(Long), note_id(Long), 음성 url(String) 보내기
//         ResponseEntity requestResult = webClient.post()
//                 .uri("/start-process")
//                 .bodyValue(noteCreateProcessReq)
//                 .retrieve()
//                 .bodyToMono(ResponseEntity.class)
//                 .block();
//
//        // 상태코드로 완료 여부 판단
//         if(requestResult.getStatusCode().is2xxSuccessful()){
//             // noteRepository.save(note);
//             ApiResponse apiResponse = ApiResponse.builder()
//                     .check(true)
//                     .information("노트 생성 성공")
//                     .build();
//
//             return ResponseEntity.ok(apiResponse);
//         }
//         else{
//             ApiResponse apiResponse = ApiResponse.builder()
//                     .check(false)
//                     .information("노트 생성 실패")
//                     .build();
//
//             return ResponseEntity.badRequest().body(apiResponse);
//         }
//     }

    // 문제지 목록 조회
    public ResponseEntity<?> getAllNotesByFolder(UserPrincipal userPrincipal, Long folderId) {
        User user = getUser(userPrincipal);
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        DefaultAssert.isTrue(user == folder.getProfessor().getUser(), "사용자가 일치하지 않습니다.");

        List<Note> notes = noteRepository.findAllByFolderOrderByCreatedAtDesc(folder);
        List<ProfessorNoteListDetailRes> noteListDetailRes = notes.stream()
                .map(note -> {
                    // int studentSize = noteStudentRepository.countByNoteAndNoteStatus(note, NoteStatus.COMPLETED);
                    // LocalDateTime endDate = note.getEndDate();
                    // boolean isClosed = endDate != null && LocalDateTime.now().isAfter(endDate);
                    return ProfessorNoteListDetailRes.builder()
                            .noteId(note.getNoteId())
                            .title(note.getTitle())
                            .createdAt(note.getCreatedAt().toLocalDate())
                            //.endDate(endDate != null ? endDate.toString() : null)
                            .practiceSize(practiceRepository.countByNote(note))
                            //.studentSize(studentSize)
                            .code(note.getCode())
                            //.average(note.getAverage())
                            //.closed(isClosed)
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

    public ResponseEntity<?> getAllNotesByFolderWithSummary(UserPrincipal userPrincipal, Long folderId) {
        User user = getUser(userPrincipal);
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        DefaultAssert.isTrue(user.equals(folder.getProfessor().getUser()), "사용자가 일치하지 않습니다.");

        // summary가 있는 노트만 조회
        List<Note> notes = noteRepository
                .findAllByFolderAndSummaryIsNotNullOrderByCreatedAtDesc(folder);

        List<ProfessorNoteListDetailRes> noteListDetailRes = notes.stream()
                .map(note -> ProfessorNoteListDetailRes.builder()
                        .noteId(note.getNoteId())
                        .title(note.getTitle())
                        .createdAt(note.getCreatedAt().toLocalDate())
                        .practiceSize(practiceRepository.countByNote(note))
                        .code(note.getCode())
                        .build())
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
        User user = getUser(userPrincipal);
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
        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .check(true)
                .information("노트 삭제 성공")
                .build();

        return ResponseEntity.ok(apiResponse);
    }


    // 문제지 랜덤 코드 생성
    @Transactional
    public ResponseEntity<?> createRandomCode(UserPrincipal userPrincipal, Long noteId) {
        getUser(userPrincipal);
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));

        // 코드 생성 시 영문자와 숫자만 포함
        String code = generateUniqueCode();

        note.updateCode(code);

        NoteCodeRes noteCodeRes = NoteCodeRes.builder()
                .code(note.getCode())
                .build();

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .check(true)
                .information(noteCodeRes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // 랜덤 코드 생성 (중복 방지)
    private String generateUniqueCode() {
        String code = generateRandomCode();
        while (noteRepository.existsByCode(code)) {
            code = generateRandomCode();
        }
        return code;
    }

    // 랜덤 코드 문자열 생성
    private String generateRandomCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(6);
        Random random = new Random();

        for (int i = 0; i < 6; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }

        return code.toString();
    }

    public ResponseEntity<?> getNoteResult(UserPrincipal userPrincipal, Long noteId) {
        getUser(userPrincipal);
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));

        List<NoteStudent> noteStudentList = noteStudentRepository.findByNote(note);
        List<NoteStudent> completedNoteStudentList = noteStudentList.stream()
                .filter(noteStudent -> noteStudent.getNoteStatus() == NoteStatus.COMPLETED)
                .toList();

        NoteResultOfAllStudentListRes noteResultOfAllStudentListRes = NoteResultOfAllStudentListRes.builder()
                .noteResultOfAllStudentDetailRes((NoteResultOfAllStudentDetailRes) completedNoteStudentList.stream()
                        .map(noteStudent -> {
                            Note findNote = noteRepository.findById(noteId)
                                    .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));
                            return NoteResultOfAllStudentDetailRes.builder()
                                    .studentNumber(noteStudent.getStudent().getStudentNumber())
                                    .studentName(noteStudent.getStudent().getName())
                                    .correctCount(noteStudentService.getCorrectCount(noteStudent.getNoteStudentId()))
                                    .totalCount(practiceRepository.countByNote(findNote))
                                    .build();
                        })
                        .toList())
                .build();


        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .check(true)
                .information(noteResultOfAllStudentListRes)
                .build();

        return ResponseEntity.ok(apiResponse);

    }

    public ResponseEntity<?> getNote(UserPrincipal userPrincipal, Long noteId) {
        User user = getUser(userPrincipal);
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));

        // 접근 권한 체크
        Folder folder = note.getFolder();
        DefaultAssert.isTrue(folder.getProfessor().getUser().equals(user), "접근 권한이 없습니다.");

        // DTO로 변환 (예시)
        NoteDetailRes noteDetailRes = NoteDetailRes.from(note);

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .check(true)
                .information(noteDetailRes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }



    private User getUser(UserPrincipal userPrincipal){
        String email = userPrincipal.getEmail();
        return userRepository.findByEmail(email).orElseThrow(()
                -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
}