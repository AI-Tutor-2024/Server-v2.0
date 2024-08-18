package com.example.ai_tutor.domain.note.application;

import com.example.ai_tutor.domain.Folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.note_student.domain.repository.NoteStudentRepository;
import com.example.ai_tutor.domain.practice.domain.repository.PracticeRepository;
import com.example.ai_tutor.domain.student.domain.repository.StudentRepository;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


}
