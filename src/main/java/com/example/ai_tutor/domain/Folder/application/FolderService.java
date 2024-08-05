package com.example.ai_tutor.domain.Folder.application;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.Folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.Folder.dto.request.FolderCreateReq;
import com.example.ai_tutor.domain.Folder.dto.response.FolderListRes;
import com.example.ai_tutor.domain.Folder.dto.response.FolderNameListRes;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
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
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> createNewFolder(UserPrincipal userPrincipal, FolderCreateReq folderCreateReq) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String folderName = folderCreateReq.getFolderName();
        String professor = folderCreateReq.getProfessor();

        Folder folder = Folder.builder()
                .folderName(folderName)
                //.professor(professor)
                //.user(user)
                .build();

        folderRepository.save(folder);
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("폴더 생성 성공")
                .build();

        return ResponseEntity.ok(apiResponse);

    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllFolders(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<Folder> folders = folderRepository.findAllByUser(user);
        List<FolderListRes> folderRes = folders.stream()
                .map(folder -> FolderListRes.builder()
                        .folderId(folder.getFolderId())
                        .folderName(folder.getFolderName())
                        //.professor(folder.getProfessor())
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
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Folder folder=folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
       // DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");

        // folder.updateFolder(folderCreateReq.getFolderName(), folderCreateReq.getProfessor());

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("폴더 정보 수정 성공")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @Transactional
    public ResponseEntity<?> deleteFolder(UserPrincipal userPrincipal, Long folderId) {
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Folder folder=folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));
        //DefaultAssert.isTrue(folder.getUser().equals(user), "해당 폴더에 접근할 수 없습니다.");

        folderRepository.delete(folder);
        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("폴더 삭제 성공")
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
