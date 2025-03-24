package com.example.ai_tutor.domain.folder.application;
import com.example.ai_tutor.domain.folder.domain.Folder;
import com.example.ai_tutor.domain.folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.folder.dto.request.FolderReq;
import com.example.ai_tutor.domain.folder.dto.response.FolderNameListRes;
import com.example.ai_tutor.domain.folder.dto.response.FolderRes;
import com.example.ai_tutor.domain.note.dto.response.FolderInfoRes;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;


    // 교수자 - 폴더 생성
    @Transactional
    public ResponseEntity<?> createNewFolder(String email, FolderReq.FolderCreateReq folderCreateReq) {
        User user = getUserByEmail(email);

        // 새로운 폴더 생성
        Folder newFolder = Folder.create(folderCreateReq);
        folderRepository.save(newFolder);

        return ResponseEntity.ok(ApiResponse.builder()
                .check(true)
                .information("폴더 생성 성공")
                .build());

    }


    // 폴더 목록 조회 (폴더명, 교수자명 포함)
    public ResponseEntity<?> getAllFolders(UserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        List<Folder> folders = folderRepository.findAllByUser(user);

        // FolderRes의 fromEntity
        List<FolderRes> folderResList =  folders.stream()
                .map( folder -> {
                    int noteCount = folder.getNotes().size();
                    return FolderRes.fromEntity(folder, noteCount);
                })
                .toList();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(folderResList)
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
                .professor(folder.getUser().getName())
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(folderInfoRes)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @Transactional
    public ResponseEntity<?> updateFolder(UserPrincipal userPrincipal, Long folderId, FolderReq.FolderUpdateReq folderCreateReq) {
        getUser(userPrincipal);
        Folder folder=folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));


        folder.updateFolder(folderCreateReq.getFolderName());

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

    private User getUser(UserPrincipal userPrincipal){
        return userRepository.findById(userPrincipal.getId()).orElseThrow(()
                -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(()
                -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }


}
