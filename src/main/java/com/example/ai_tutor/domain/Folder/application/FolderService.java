package com.example.ai_tutor.domain.Folder.application;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.Folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.Folder.dto.request.FolderCreateReq;
import com.example.ai_tutor.domain.Folder.dto.response.FolderListRes;
import com.example.ai_tutor.domain.Folder.dto.response.FolderNameListRes;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public ResponseEntity<?> createNewFolder(UserPrincipal userPrincipal, FolderCreateReq folderCreateReq) {
        Optional<User> optionalUser = userRepository.findById(Long.valueOf(userPrincipal.getName()));
        User user= optionalUser.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String folderName = folderCreateReq.getFolderName();
        String professor = folderCreateReq.getProfessor();

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

    public ResponseEntity<?> getFolderNames(UserPrincipal userPrincipal){
        User user = userRepository.findById(Long.valueOf(userPrincipal.getName())).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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
        User user = userRepository.findById(Long.valueOf(userPrincipal.getName())).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
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
        User user = userRepository.findById(Long.valueOf(userPrincipal.getName())).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new IllegalArgumentException("폴더를 찾을 수 없습니다."));

        if(!Objects.equals(folder.getUser().getUserId(), user.getUserId())) { throw new IllegalArgumentException("폴더를 수정할 권한이 없습니다."); }

        String folderName = folderCreateReq.getFolderName();
        String professor = folderCreateReq.getProfessor();
        folder.updateFolder(folderName, professor);
        folderRepository.save(folder);

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information("폴더 수정 성공")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

}
