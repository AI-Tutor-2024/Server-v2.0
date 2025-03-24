package com.example.ai_tutor.domain.folder.presentation;

import com.example.ai_tutor.domain.folder.dto.request.FolderReq;
import com.example.ai_tutor.global.BaseIntegrationTest;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@ActiveProfiles("test")
@DisplayName("FolderController 통합 테스트")
@SpringBootTest
public class FolderControllerTest extends BaseIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;

    @BeforeEach
    void ensureBaseIntegrationTestRanAgain() throws Exception {
        super.setUp();  // 부모 클래스의 @BeforeEach 강제 실행

        System.out.println("[FolderControllerTest] @BeforeEach 실행됨");
        log.info("[FolderControllerTest] @BeforeEach 실행됨");

        assertNotNull(accessToken, "accessToken이 null 입니다. BaseIntegrationTest가 실행되지 않았을 가능성이 있습니다.");
    }


    @Nested
    @DisplayName("폴더 생성 API")
    class CreateNewFolderTest {

        @Test
        @DisplayName("성공적으로 폴더를 생성해야 한다")
        void createNewFolderSuccess() throws Exception {
            log.info("createNewFolderSuccess() 테스트 시작...");
            // Given
            // 폴더 생성 요청 객체 생성
            FolderReq.FolderCreateReq folderCreateReq = FolderReq.FolderCreateReq.builder()
                    .folderName("데이터베이스")
                    .build();

            // When: 폴더 생성 요청 API 호출
            ResultActions resultActions = requestCreateFolder(folderCreateReq);

            // Then: 응답 결과 validate
            validateCreateFolderResponse(resultActions);
        }

        // 폴더 생성 API 요청 수행
        private ResultActions requestCreateFolder(FolderReq.FolderCreateReq request) throws Exception {
            log.info("requestCreateFolder 시작...");
            log.info("AccessToken: {}", accessToken);

            return mockMvc.perform(post("/api/v1/folder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andDo(print());
        }

        // API 응답 검증
        private void validateCreateFolderResponse(ResultActions resultActions) throws Exception {
            resultActions
                    .andExpectAll(
                            status().isOk(),  // HTTP 200 응답 검증
                            jsonPath("$.check").value(true),  // API 응답값 검증
                            jsonPath("$.information").value("폴더 생성 성공") // 성공 메시지 검증
                    )
                    .andExpect(header().exists("Location")); // 응답 헤더에 "Location" 포함 여부 확인
        }


    }
}
