package com.example.ai_tutor.domain.note.application;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.Folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.note.dto.request.NoteAccessReq;
import com.example.ai_tutor.domain.note.dto.request.NoteStepUpdateReq;
import com.example.ai_tutor.domain.note.dto.response.NoteAccessRes;
import com.example.ai_tutor.domain.note.dto.response.StudentNoteListDetailRes;
import com.example.ai_tutor.domain.note.dto.response.NoteListRes;
import com.example.ai_tutor.domain.note_student.domain.NoteStudent;
import com.example.ai_tutor.domain.note_student.domain.repository.NoteStudentRepository;
import com.example.ai_tutor.domain.practice.domain.repository.PracticeRepository;
import com.example.ai_tutor.domain.student.domain.Student;
import com.example.ai_tutor.domain.student.domain.repository.StudentRepository;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentNoteService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final StudentRepository studentRepository;
    private final FolderRepository folderRepository;
    private final PracticeRepository practiceRepository;
    private final NoteStudentRepository noteStudentRepository;

    public ResponseEntity<?> accessNoteByCode(NoteAccessReq noteAccessReq) {

        Note note = (Note) noteRepository.findByCode(noteAccessReq.getCode()).orElseThrow(() -> new IllegalArgumentException("문제지를 찾을 수 없습니다."));
        boolean isClosed = LocalDateTime.now().isAfter(note.getEndDate());

        if (isClosed) {
            return ResponseEntity.badRequest().body("문제지 접근 기간이 만료되었습니다.");
        }
        else{
            // 문제지 id를 반환
            NoteAccessRes noteAccessRes = NoteAccessRes.builder()
                    .noteId(note.getNoteId())
                    .build();

            return ResponseEntity.ok(noteAccessRes);
        }
    }

    // 문제지 목록 조회
//    public ResponseEntity<?> getAllNotes(UserPrincipal userPrincipal, Long folderId) {
//        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//        Student student = studentRepository.findByUser(user);

//        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        //DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");

//        List<Note> notes = noteRepository.findAllByFolder(folder);
//        List<StudentNoteListDetailRes> studentNoteListDetailRes = notes.stream()
//                .map(note -> {
//                    boolean isClosed = LocalDateTime.now().isAfter(note.getEndDate());
//                    NoteStudent noteStudent = noteStudentRepository.findByNoteAndStudent(note, student);
//                    return StudentNoteListDetailRes.builder()
//                            .noteId(note.getNoteId())
//                            .title(note.getTitle())
//                            .practiceSize(practiceRepository.countByNote(note))
//                            .createdAt(noteStudent.getCreatedAt())
//                            .closed(isClosed)
//                            .score(noteStudent.getScore())
//                            .noteStatus(noteStudent.getNoteStatus().toString())
//                            .build();
//                })
//                .collect(Collectors.toList());

//        NoteListRes<StudentNoteListDetailRes> noteListRes = NoteListRes.<StudentNoteListDetailRes>builder()
//                .folderName(folder.getFolderName())
//                .professor(folder.getProfessor().getUser().getName())
//                .noteListDetailRes(studentNoteListDetailRes)
//                .build();

//        return ResponseEntity.ok(noteListRes);
//    }

//    @Transactional
//    public ResponseEntity<?> updateNoteStep(UserPrincipal userPrincipal, Long noteId, NoteStepUpdateReq noteStepUpdateReq) {
//        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//        Long folderId = noteStepUpdateReq.getFolderId();
//        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
//        //DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");

//        Note note=noteRepository.findById(noteId).orElseThrow(()->new IllegalArgumentException("노트를 찾을 수 없습니다."));
//        note.updateStep(noteStepUpdateReq.getStep());

//        ApiResponse apiResponse = ApiResponse.builder()
//                .check(true)
//                .information("학습 단계 업데이트 성공")
//                .build();

//        return ResponseEntity.ok(apiResponse);
//    }

//     public ResponseEntity<?> getStepOne(UserPrincipal userPrincipal, Long noteId) {
//        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//        Note note = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));
//        DefaultAssert.isTrue(note.getUser().equals(user), "해당 노트에 접근할 수 없습니다.");
//
//        List<Text> text = textRepository.findAllByNote(note);
//        List<Summary> summary = summaryRepository.findAllByNote(note);

//        // summaryId 기준으로 정렬
//        List<Text> sortedText = text.stream()
//                .sorted(Comparator.comparing(t -> summary.stream()
//                        .filter(s -> s.getSummaryId().equals(t.getTextId()))
//                        .findFirst()
//                        .map(Summary::getSummaryId)
//                        .orElse(null)))
//                .collect(Collectors.toList());

        // textId를 순차적으로 부여
//        AtomicInteger counter = new AtomicInteger(1);
//        List<StepOneRes> stepOneRes = sortedText.stream()
//                .map(t -> StepOneRes.builder()
//                        .textId(counter.getAndIncrement()) // 1부터 증가
//                        .content(t.getContent())
//                        .summaryId(summary.stream()
//                                .filter(s -> s.getSummaryId().equals(t.getTextId()))
//                                .findFirst()
//                                .map(Summary::getSummaryId)
//                                .orElse(null))
//                        .summary(summary.stream()
//                                .filter(s -> s.getSummaryId().equals(t.getTextId()))
//                                .findFirst()
//                                .map(Summary::getContent)
//                                .orElse(null))
//                        .build())
//                .collect(Collectors.toList());

//         StepOneListRes stepOneListRes = StepOneListRes.builder()
                //.stepOneRes(stepOneRes)
//                 .build();

//         return ResponseEntity.ok(stepOneListRes);


//     }

}
