package com.example.ai_tutor.domain.note.application;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.Folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.Repository.NoteRepository;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateReq;
import com.example.ai_tutor.domain.note.dto.request.NoteDeleteReq;
import com.example.ai_tutor.domain.note.dto.response.NoteListDetailRes;
import com.example.ai_tutor.domain.note.dto.response.NoteListRes;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
    private final FolderRepository FolderRepository;
    private final AmazonS3 amazonS3;

    public ResponseEntity<?> createNewNote(UserPrincipal userPrincipal, Long folderId, NoteCreateReq noteCreateReq){
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Folder folder = FolderRepository.findAllByUser(user).stream()
                .filter(f -> f.getFolderId().equals(folderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        MultipartFile recordFile = noteCreateReq.getRecordFile();
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

    public ResponseEntity<?> getAllNotes(UserPrincipal userPrincipal, Long folderId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Folder folder= FolderRepository.findAllByUser(user).stream()
                .filter(f -> f.getFolderId().equals(folderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

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

    public ResponseEntity<?> deleteNoteById(UserPrincipal userPrincipal, NoteDeleteReq noteDeleteReq, Long noteId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Long folderId = noteDeleteReq.getFolderId();
        Folder folder= FolderRepository.findAllByUser(user).stream()
                .filter(f -> f.getFolderId().equals(folderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        List <Note> notes = noteRepository.findAllByFolder(folder);
        Note note = notes.stream()
                .filter(n -> n.getNoteId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new  IllegalArgumentException("노트를 찾을 수 없습니다."));

        noteRepository.delete(note);
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("노트 삭제 성공")
                .build();

        return ResponseEntity.ok(apiResponse);

    }
}
