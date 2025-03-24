package com.example.ai_tutor.domain.folder.application;

import com.example.ai_tutor.domain.folder.domain.repository.FolderRepository;
import com.example.ai_tutor.domain.folder.dto.request.FolderReq;
import com.example.ai_tutor.domain.user.domain.Provider;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("FolderService Test")
public class FolderServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FolderRepository folderRepository;

    @InjectMocks
    private FolderService folderService;

    private UserPrincipal mockUserPrincipal;
    private User mockUser;

    @BeforeEach
    void setUp() {
        // 1. Mock User 생성
        mockUser = User.builder()
                .name("테스트 교수")
                .email("test@ai-tutor.com")
                .password("password")
                .provider(Provider.valueOf("google"))
                .providerId("google_12345")
                .build();



        // 4. UserPrincipal Mock 객체 생성
        mockUserPrincipal = UserPrincipal.create(mockUser);

        // 5. Mock 객체 stubbing -> 실제 DB 조회 없이 테스트 가능
        // userRepository.findById(1L) 호출 시 mockUser 객체 반환 (User, Professor, Folder)

        // stubbing이 없으면, userRepository.findById(1L)에서 null을 반환하게 됨
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
    }

    @Nested
    @DisplayName("폴더 생성")
    class CreateNewFolder {  // 내부 클래스는 static이어야 하나, JUnit5부터는 static이 아니어도 됨

        @Test
        @DisplayName("성공적으로 폴더를 생성해야 한다")
        void createNewFolderSuccess() {
            // given
            // 폴더 생성 요청 객체 생성
            FolderReq.FolderCreateReq folderCreateReq = new FolderReq.FolderCreateReq("테스트 폴더");

            // when
            // 폴더 생성 요청
            ResponseEntity<?> response = folderService.createNewFolder(mockUserPrincipal.getUsername(), folderCreateReq);

            // then
            // 폴더 생성 성공
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }
}
