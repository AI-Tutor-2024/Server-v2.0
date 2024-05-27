package com.example.ai_tutor.domain.note.application;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.Folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateReq;
import com.example.ai_tutor.domain.note.dto.request.NoteDeleteReq;
import com.example.ai_tutor.domain.note.dto.request.NoteStepUpdateReq;
import com.example.ai_tutor.domain.note.dto.response.NoteListDetailRes;
import com.example.ai_tutor.domain.note.dto.response.NoteListRes;
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
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final FolderRepository folderRepository;
    private final AmazonS3 amazonS3;

    @Transactional
    public ResponseEntity<?> createNewNote(UserPrincipal userPrincipal, Long folderId, NoteCreateReq noteCreateReq, MultipartFile recordFile) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");

        String fileName= UUID.randomUUID().toString();
        try {
            amazonS3.putObject(new PutObjectRequest("ai-tutor-record", fileName, recordFile.getInputStream(), null));
        } catch (IOException e) { throw new RuntimeException(e); }
        
        String recordUrl = amazonS3.getUrl("ai-tutor-record", fileName).toString();
        Note note = Note.builder()
                .title(noteCreateReq.getTitle())
                .recordUrl(recordUrl)
                .step(0)
                .folder(folder)
                .user(user)
                .build();

        noteRepository.save(note);
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("노트 생성 성공")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllNotes(UserPrincipal userPrincipal, Long folderId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");

        List <Note> notes = noteRepository.findAllByFolder(folder);
        List<NoteListDetailRes> noteListDetailRes = notes.stream()
                .map(note -> NoteListDetailRes.builder()
                        .title(note.getTitle())
                        .step(note.getStep())
                        .createdAt(note.getCreatedAt())
                        .length(note.getLength())
                        .build())
                .collect(Collectors.toList());

        NoteListRes noteListRes = NoteListRes.builder()
                .folderName(folder.getFolderName())
                .professor(folder.getProfessor())
                .noteListDetailRes(noteListDetailRes)
                .build();

        return ResponseEntity.ok(noteListRes);
    }

    @Transactional
    public ResponseEntity<?> deleteNoteById(UserPrincipal userPrincipal, NoteDeleteReq noteDeleteReq, Long noteId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Long folderId = noteDeleteReq.getFolderId();

        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));
        DefaultAssert.isTrue(note.getFolder().equals(folder), "해당 노트에 접근할 수 없습니다.");

        noteRepository.delete(note);
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("노트 삭제 성공")
                .build();

        return ResponseEntity.ok(apiResponse);

    }

    @Transactional
    public ResponseEntity<?> updateNoteStep(UserPrincipal userPrincipal, Long noteId, NoteStepUpdateReq noteStepUpdateReq) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Long folderId = noteStepUpdateReq.getFolderId();
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");

        Note note=noteRepository.findById(noteId).orElseThrow(()->new IllegalArgumentException("노트를 찾을 수 없습니다."));
        note.updateStep(noteStepUpdateReq.getStep());

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("학습 단계 업데이트 성공")
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
