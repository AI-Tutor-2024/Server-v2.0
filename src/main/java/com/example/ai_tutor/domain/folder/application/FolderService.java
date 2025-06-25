package com.example.ai_tutor.domain.folder.application;
import com.example.ai_tutor.domain.folder.domain.Folder;
import com.example.ai_tutor.domain.folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.folder.dto.response.*;
import com.example.ai_tutor.domain.folder.dto.request.FolderCreateReq;
import com.example.ai_tutor.domain.note.dto.response.FolderInfoRes;
import com.example.ai_tutor.domain.professor.domain.Professor;
import com.example.ai_tutor.domain.professor.domain.repository.ProfessorRepository;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final ProfessorRepository professorRepository;


    // 교수자 - 폴더 생성
    @Transactional
    public ResponseEntity<?> createNewFolder( UserPrincipal userPrincipal, FolderCreateReq folderCreateReq) {
        User user = getUser(userPrincipal);
        String folderName = folderCreateReq.getFolderName();
        String professorName = folderCreateReq.getProfessorName();

        Professor professor = user.getProfessor();
        if(Objects.isNull(professor)){
            professor = Professor.builder()
                    .professorName(user.getName())
                    .user(user)
                    .build();
            professorRepository.save(professor);
            user.updateProfessor(professor);
        }

        Folder folder = Folder.builder()
                .folderName(folderName)
                .professor(professor)
                .professorName(professorName)
                .build();

        folderRepository.save(folder);

        ApiResponse<Object> apiResponse = ApiResponse.builder()
                .check(true)
                .information(FolderResponse.from(folder))
                .build();

        return ResponseEntity.ok(apiResponse);

    }

    // 폴더 이름 목록 조회
    public ResponseEntity<?> getFolderNames(UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        Professor professor = user.getProfessor();
        List<Folder> folders = professor.getFolders();
        List<FolderNameListRes> folderRes = folders.stream()
                .map(folder -> FolderNameListRes.builder()
                        .folderId(folder.getFolderId())
                        .folderName(folder.getFolderName())
                        .build(
                ))
                .toList();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(folderRes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // 폴더 목록 조회 (폴더명, 교수자명 포함)
    public ResponseEntity<?> getAllFolders(UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        Professor professor = user.getProfessor();
        List<Folder> folders = professor.getFolders();
        List<FolderListRes> folderRes = folders.stream()
                .map(folder -> FolderListRes.builder()
                        .folderId(folder.getFolderId())
                        .noteCount(folder.getNotes().size())
                        .folderName(folder.getFolderName())
                        .professor(folder.getProfessorName())
                        .build(
                        ))
                .toList();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(folderRes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // 수업 정보 조회
    @Transactional
    public ResponseEntity<?> getFolderInfo(UserPrincipal userPrincipal, Long folderId) {
        getUser(userPrincipal);
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        FolderInfoRes folderInfoRes = FolderInfoRes.builder()
                .folderName(folder.getFolderName())
                .professor(folder.getProfessor().getUser().getName())
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(folderInfoRes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @Transactional
    public ResponseEntity<?> updateFolder(UserPrincipal userPrincipal, Long folderId, FolderCreateReq folderCreateReq) {
        getUser(userPrincipal);
        Folder folder=folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));


        folder.updateFolder(folderCreateReq.getFolderName(), folderCreateReq.getProfessorName());

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("폴더 정보 수정 성공")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @Transactional
    public ResponseEntity<?> deleteFolder(UserPrincipal userPrincipal, Long folderId) {
        getUser(userPrincipal);
        Folder folder=folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        folderRepository.delete(folder);
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("폴더 삭제 성공")
                .build();

        return ResponseEntity.ok(apiResponse);

    }


    public FolderAndNoteListRes getFolderAndNoteList(UserPrincipal userPrincipal) {
        validateUser(userPrincipal);
        Professor professor = validateProfessor(userPrincipal);

        List<Folder> folders = professor.getFolders();
        List<FolderAndNoteDetailRes> folderNoteDetailList = mapToFolderAndNoteDetailResList(folders);

        return FolderAndNoteListRes.builder()
                .folderCount(folderNoteDetailList.size())
                .folderNoteDetailList(folderNoteDetailList)
                .build();
    }

    private List<FolderAndNoteDetailRes> mapToFolderAndNoteDetailResList(List<Folder> folders) {
        return folders.stream()
                .map(folder -> {
                    List<FolderAndNoteDetailRes.NotesInFolderRes> notes = folder.getNotes().stream()
                            .map(note -> FolderAndNoteDetailRes.NotesInFolderRes.from(
                                    note.getNoteId(),
                                    note.getTitle()
                            ))
                            .collect(Collectors.toList());

                    return FolderAndNoteDetailRes.from(
                            folder.getFolderId(),
                            folder.getFolderName(),
                            notes
                    );
                })
                .collect(Collectors.toList());
    }


    private User getUser(UserPrincipal userPrincipal){
        return userRepository.findById(userPrincipal.getId()).orElseThrow(()
                -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private User validateUser(UserPrincipal userPrincipal) {
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private Professor validateProfessor(UserPrincipal userPrincipal) {
        return professorRepository.findByUser(userRepository.findById(userPrincipal.getId())
                        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다.")))
                .orElseThrow(() -> new IllegalArgumentException("교수를 찾을 수 없습니다."));
    }

}
