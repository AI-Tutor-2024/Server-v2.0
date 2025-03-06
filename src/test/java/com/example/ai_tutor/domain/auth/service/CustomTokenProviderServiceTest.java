package com.example.ai_tutor.domain.auth.service;
import com.example.ai_tutor.domain.auth.application.CustomTokenProviderService;
import com.example.ai_tutor.domain.auth.application.CustomUserDetailsService;
import com.example.ai_tutor.domain.auth.dto.TokenMapping;
import com.example.ai_tutor.global.JwtTestUtil;
import com.example.ai_tutor.global.config.security.OAuth2Config;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Test")
class CustomTokenProviderServiceTest {

    @InjectMocks
    private CustomTokenProviderService tokenProvider;

    @Mock
    private OAuth2Config oAuth2Config;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    private Long testUserId = 1L;  // 테스트 User ID
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트용 사용자 설정
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(String.valueOf(testUserId));

        // 토큰 생성 (userId 기반)
        accessToken = JwtTestUtil.generateJwtToken(testUserId);
        refreshToken = JwtTestUtil.generateJwtToken(testUserId);
    }

    @Test
    @DisplayName("JWT AccessToken & RefreshToken 생성 테스트")
    void testGenerateJwtToken() {
        TokenMapping tokenMapping = tokenProvider.createToken(authentication);

        assertNotNull(tokenMapping);
        assertNotNull(tokenMapping.getAccessToken());
        assertNotNull(tokenMapping.getRefreshToken());

        log.info("AccessToken: {}", tokenMapping.getAccessToken());
        log.info("RefreshToken: {}", tokenMapping.getRefreshToken());
    }

    @Test
    @DisplayName("RefreshToken으로 새 AccessToken 생성 테스트")
    void testRefreshToken() {
        TokenMapping oldTokenMapping = tokenProvider.createToken(authentication);
        TokenMapping newTokenMapping = tokenProvider.refreshToken(authentication, oldTokenMapping.getRefreshToken());

        assertNotNull(newTokenMapping.getAccessToken());
        assertEquals(oldTokenMapping.getRefreshToken(), newTokenMapping.getRefreshToken());
    }

    @Test
    @DisplayName("JWT 토큰 검증 테스트 (유효한 토큰)")
    void testValidateToken_Success() {
        assertTrue(tokenProvider.validateToken(accessToken));
    }

    @Test
    @DisplayName("만료된 JWT 토큰 검증 실패")
    void testValidateToken_Expired() {
        // 인위적으로 만료된 토큰 생성
        String expiredToken = JwtTestUtil.generateExpiredJwtToken(testUserId);
        assertThrows(ExpiredJwtException.class, () -> tokenProvider.validateToken(expiredToken));
    }

    @Test
    @DisplayName("잘못된 서명 JWT 토큰 검증 실패")
    void testValidateToken_InvalidSignature() {
        // 잘못된 SecretKey로 만든 토큰
        String invalidToken = JwtTestUtil.generateJwtToken(9999L); // 올바르지 않은 userId 사용
        assertThrows(SignatureException.class, () -> tokenProvider.validateToken(invalidToken));
    }

    @Test
    @DisplayName("JWT에서 UserId 추출 테스트")
    void testGetUserIdFromToken() {
        Long userId = tokenProvider.getUserIdFromToken(accessToken);

        assertNotNull(userId);
        assertEquals(testUserId, userId);
    }

    @Test
    @DisplayName("UserId 기반 Authentication 객체 생성")
    void testGetAuthenticationById() {
        when(customUserDetailsService.loadUserById(testUserId)).thenReturn(userDetails);

        UsernamePasswordAuthenticationToken authentication = tokenProvider.getAuthenticationById(accessToken);

        assertNotNull(authentication);
        assertEquals(userDetails, authentication.getPrincipal());
    }
}
