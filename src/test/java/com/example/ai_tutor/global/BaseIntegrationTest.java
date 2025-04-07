package com.example.ai_tutor.global;

import com.example.ai_tutor.domain.auth.application.CustomTokenProviderService;
import com.example.ai_tutor.domain.auth.dto.SignInReq;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@Disabled
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@Slf4j
@SpringBootTest
public class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected CustomTokenProviderService tokenProvider;

    protected String accessToken;

    @BeforeEach
    void checkBaseIntegrationTestExecution() {
        System.out.println("[BaseIntegrationTest] @BeforeEach 실행됨");
        log.info("[BaseIntegrationTest] @BeforeEach 실행됨");
    }


    @BeforeEach
    public void setUp() throws Exception {
        log.info("setUp() 실행 중...");

        accessToken = JwtTestUtil.generateJwtToken(1L);  // User ID 기반
        log.info("초기 생성된 JWT: {}", accessToken);

        // 1. 가짜 구글 로그인 요청 데이터 생성
        SignInReq googleLoginRequest = new SignInReq(
                "test1@gmail.com",
                "234587198"
        );

        // 2. 서비스의 로그인 API 호출 -> accessToken 발급
        MvcResult loginResult = mockMvc.perform(post("/auth/sign-in")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(googleLoginRequest)))
                .andDo(print())  // 응답 디버깅
                .andExpect(status().isOk()) // 기대하는 상태코드
                .andReturn();

        // 3. 응답에서 accessToken 추출
        String responseBody = loginResult.getResponse().getContentAsString();
        log.info("로그인 API 응답 바디: {}", responseBody);

        accessToken = objectMapper.readTree(responseBody).get("accessToken").asText();
        log.info("로그인 후 받은 AccessToken: {}", accessToken);
    }


    // accessToken이 정상적으로 생성되었는지 확인
    @BeforeEach
    void checkAccessToken() {
        log.info("checkAccessToken() 실행 중...");

        assertNotNull(accessToken, "AccessToken이 null 입니다. 로그인 API 호출이 정상적으로 수행되지 않았을 가능성이 있습니다.");

        log.info("생성된 AccessToken: {}", accessToken);

        // JWT 검증 실행
        boolean isValid = tokenProvider.validateToken(accessToken);

        if (!isValid) {
            log.error("AccessToken이 유효하지 않습니다.");
        } else {
            log.info("AccessToken이 유효합니다.");
        }

        assertTrue(isValid, "생성된 AccessToken이 유효하지 않습니다.");
    }


}
