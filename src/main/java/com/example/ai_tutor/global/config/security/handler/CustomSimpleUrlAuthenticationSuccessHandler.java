package com.example.ai_tutor.global.config.security.handler;

import com.example.ai_tutor.domain.auth.application.JwtUtil;
import com.example.ai_tutor.domain.auth.domain.Token;
import com.example.ai_tutor.domain.auth.domain.repository.CustomAuthorizationRequestRepository;
import com.example.ai_tutor.domain.auth.domain.repository.TokenRepository;
import com.example.ai_tutor.domain.auth.dto.TokenMapping;
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

        // 3) 세션에서 redirec_url 가져오기
//        String redirectUrl = (String) request.getSession().getAttribute("OAUTH2_REDIRECT_URL");
        Optional<String> redirectUri = CustomCookie.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);
        String redirectUrl = redirectUri.orElse("https://ai-tutor.ac.kr/");

        log.info("redirectUrl: {}", redirectUrl);

        // accessToken과 refreshToken을 쿼리 파라미터로 추가
        redirectUrl = this.setRedirectUrl(redirectUrl, tokenMapping.getAccessToken(), tokenMapping.getRefreshToken());

        // 리디렉션 처리 전에 쿠키 제거
        customAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        // 리디렉션 처리
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        log.info("리디렉션 완료 - URL: {}", redirectUrl);
    }

    private String setRedirectUrl(String redirectUrl, String accessToken, String refreshToken) {
        return UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();
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

