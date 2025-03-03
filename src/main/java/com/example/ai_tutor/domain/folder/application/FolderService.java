package com.example.ai_tutor.domain.folder.application;

import com.example.ai_tutor.domain.folder.domain.Folder;
import com.example.ai_tutor.domain.folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.folder.dto.request.FolderCreateReq;
import com.example.ai_tutor.domain.folder.dto.response.FolderListRes;
import com.example.ai_tutor.domain.folder.dto.response.FolderNameListRes;
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
        // User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User user = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

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

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("폴더 생성 성공")
                .build();

        return ResponseEntity.ok(apiResponse);

    }

    // 폴더 이름 목록 조회
    public ResponseEntity<?> getFolderNames(UserPrincipal userPrincipal) {
        // User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User user = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

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
        // User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User user = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Professor professor = user.getProfessor();
        List<Folder> folders = professor.getFolders();
        List<FolderListRes> folderRes = folders.stream()
                .map(folder -> FolderListRes.builder()
                        .folderId(folder.getFolderId())
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

    @Transactional
    public ResponseEntity<?> updateFolder(UserPrincipal userPrincipal, Long folderId, FolderCreateReq folderCreateReq) {
        // User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User user = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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
        // User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        User user = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Folder folder=folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        folderRepository.delete(folder);
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("폴더 삭제 성공")
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
