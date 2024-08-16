package com.example.ai_tutor.domain.note.application;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.Folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.note.domain.Note;
import com.example.ai_tutor.domain.note.domain.repository.NoteRepository;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateProcessReq;
import com.example.ai_tutor.domain.note.dto.request.NoteCreateReq;
import com.example.ai_tutor.domain.note.dto.request.NoteDeleteReq;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfessorNoteService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final FolderRepository folderRepository;

    private final AmazonS3 amazonS3;
    private final WebClient webClient;

    @Transactional
    public ResponseEntity<?> createNewNote(UserPrincipal userPrincipal, Long folderId, NoteCreateReq noteCreateReq, MultipartFile recordFile) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        //DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");

        String fileName= UUID.randomUUID().toString();
        try {
            amazonS3.putObject(new PutObjectRequest("ai-tutor-record", fileName, recordFile.getInputStream(), null));
        } catch (IOException e) { throw new RuntimeException(e); }

        String recordUrl = amazonS3.getUrl("ai-tutor-record", fileName).toString();
        Note note = Note.builder()
                .title(noteCreateReq.getTitle())
                //.recordUrl(recordUrl)
                .step(0)
                .folder(folder)
                //.user(user)
                .build();

        NoteCreateProcessReq noteCreateProcessReq = NoteCreateProcessReq.builder()
                .userId(user.getUserId())
                .folderId(folderId)
                .noteId(note.getNoteId())
                .recordUrl(recordUrl)
                .build();

        //post요청으로 user_id(Long), folder_id(Long), note_id(Long), 음성 url(String) 보내기
        ResponseEntity requestResult = webClient.post()
                .uri("/start-process")
                .bodyValue(noteCreateProcessReq)
                .retrieve()
                .bodyToMono(ResponseEntity.class)
                .block();

        //상태코드로 완료 여부 판단
        if(requestResult.getStatusCode().is2xxSuccessful()){
            noteRepository.save(note);
            ApiResponse apiResponse = ApiResponse.builder()
                    .check(true)
                    .information("노트 생성 성공")
                    .build();

            return ResponseEntity.ok(apiResponse);
        }
        else{
            ApiResponse apiResponse = ApiResponse.builder()
                    .check(false)
                    .information("노트 생성 실패")
                    .build();

            return ResponseEntity.badRequest().body(apiResponse);
        }
    }

    // 문제지 삭제
    @Transactional
    public ResponseEntity<?> deleteNoteById(UserPrincipal userPrincipal, NoteDeleteReq noteDeleteReq, Long noteId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Long folderId = noteDeleteReq.getFolderId();

        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        //DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new IllegalArgumentException("노트를 찾을 수 없습니다."));
        DefaultAssert.isTrue(note.getFolder().equals(folder), "해당 노트에 접근할 수 없습니다.");

        // practice 먼저 삭제
        // practice랑 연결된 answer도 삭제

        noteRepository.delete(note);
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("노트 삭제 성공")
                .build();

        return ResponseEntity.ok(apiResponse);

    }

}
