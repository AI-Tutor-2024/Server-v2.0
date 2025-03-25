package com.example.ai_tutor.global.config.security.handler;

import com.example.ai_tutor.domain.auth.application.JwtUtil;
import com.example.ai_tutor.domain.auth.domain.Token;
import com.example.ai_tutor.domain.auth.domain.repository.CustomAuthorizationRequestRepository;
import com.example.ai_tutor.domain.auth.domain.repository.TokenRepository;
import com.example.ai_tutor.domain.auth.dto.TokenMapping;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.OAuth2Config;
import com.example.ai_tutor.global.config.security.util.CustomCookie;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static com.example.ai_tutor.domain.auth.domain.repository.CustomAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomSimpleUrlAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;             // JWT 생성 유틸
    private final TokenRepository tokenRepository;
    private final CustomAuthorizationRequestRepository customAuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 이미 응답이 전송된 상태인지 확인
        if (response.isCommitted()) {
            log.warn("Response already committed.");
            return;
        }

        // 1) JWT 토큰 생성
        TokenMapping tokenMapping = jwtUtil.createToken(authentication);

        // 2) RefreshToken 저장 (DB or Redis etc.)
        Token token = Token.builder()
                .userEmail(tokenMapping.getUserEmail())
                .refreshToken(tokenMapping.getRefreshToken())
                .build();
        tokenRepository.save(token);

        // 3) JSON으로 응답
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"accessToken\":\"%s\",\"refreshToken\":\"%s\"}",
                tokenMapping.getAccessToken(),
                tokenMapping.getRefreshToken()
        );

        // 4) OAuth2 인증 과정에서 사용된 쿠키/세션 정리
        clearAuthenticationAttributes(request, response);
        customAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        // 5) 최종 응답
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();

        log.info("OAuth2 로그인 성공. JWT 토큰 발급 후 JSON으로 응답 완료.");
    }

    /**
     * 원래 SimpleUrlAuthenticationSuccessHandler에는
     * clearAuthenticationAttributes(HttpServletRequest request) 만 있으므로,
     * 필요시 우리가 (request, response) 시그니처의 메서드로 확장.
     * 내부에서는 부모 메서드(super) 호출해서 세션 정리만 수행.
     */
    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        // 부모 클래스가 세션의 "SPRING_SECURITY_LAST_EXCEPTION" 제거해줌
        super.clearAuthenticationAttributes(request);
    }
}

