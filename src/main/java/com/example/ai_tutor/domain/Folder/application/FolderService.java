package com.example.ai_tutor.domain.Folder.application;

import com.example.ai_tutor.domain.Folder.domain.Folder;
import com.example.ai_tutor.domain.Folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.Folder.dto.request.FolderCreateReq;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FolderService {
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    public void createNewFolder(UserPrincipal userPrincipal, FolderCreateReq folderCreateReq) {
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
    }
}
