package com.example.ai_tutor.domain.Folder.application;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.Folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.Folder.dto.request.FolderCreateReq;
import com.example.ai_tutor.domain.Folder.dto.response.FolderListRes;
import com.example.ai_tutor.domain.Folder.dto.response.FolderNameListRes;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public ResponseEntity<?> createNewFolder(UserPrincipal userPrincipal, FolderCreateReq folderCreateReq) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String folderName = folderCreateReq.getFolderName();
        String professor = folderCreateReq.getProfessor();

        // 폴더 이름과 교수 이름이 null인 경우 || request body가 올바르지 않은 경우
        if(folderName == null || professor == null){
            ApiResponse apiResponse = ApiResponse.builder()
                    .check(false)
                    .information("request body 값을 확인해주세요.")
                    .build();
            return ResponseEntity.badRequest().body(apiResponse);
        } else{
            Folder folder = Folder.builder()
                    .folderName(folderName)
                    .professor(professor)
                    .user(user)
                    .build();

            folderRepository.save(folder);
            ApiResponse apiResponse = ApiResponse.builder()
                    .check(true)
                    .information("폴더 생성 성공")
                    .build();

            return ResponseEntity.ok(apiResponse);
        }
    }

    public ResponseEntity<?> getFolderNames(UserPrincipal userPrincipal){
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<Folder> folders = folderRepository.findAllByUser(user);
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

    public ResponseEntity<?> getAllFolders(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<Folder> folders = folderRepository.findAllByUser(user);
        List<FolderListRes> folderRes = folders.stream()
                .map(folder -> FolderListRes.builder()
                        .folderId(folder.getFolderId())
                        .folderName(folder.getFolderName())
                        .professor(folder.getProfessor())
                        .build(
                        ))
                .toList();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(folderRes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    public ResponseEntity<?> updateFolder(UserPrincipal userPrincipal, Long folderId, FolderCreateReq folderCreateReq) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Folder folder=folderRepository.findAllByUser(user).stream()
                .filter(f -> Objects.equals(f.getFolderId(), folderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다.")
                );

        String folderName = folderCreateReq.getFolderName();
        String professor = folderCreateReq.getProfessor();

        // 폴더 이름과 교수 이름이 null인 경우 || request body가 올바르지 않은 경우
        if(folderName == null || professor == null){
            ApiResponse apiResponse = ApiResponse.builder()
                    .check(false)
                    .information("request body 값을 확인해주세요.")
                    .build();
            return ResponseEntity.badRequest().body(apiResponse);
        }else{
            folder.updateFolder(folderName, professor);
            folderRepository.save(folder);

            ApiResponse apiResponse = ApiResponse.builder()
                    .check(true)
                    .information("폴더 수정 성공")
                    .build();

            return ResponseEntity.ok(apiResponse);
        }
    }

    public ResponseEntity<?> deleteFolder(UserPrincipal userPrincipal, Long folderId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Folder folder=folderRepository.findAllByUser(user).stream()
                .filter(f -> Objects.equals(f.getFolderId(), folderId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다.")
                );

        folderRepository.delete(folder);
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("폴더 삭제 성공")
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
