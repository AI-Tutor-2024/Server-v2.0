package com.example.ai_tutor.global.config.security.handler;

import com.example.ai_tutor.domain.auth.domain.repository.CustomAuthorizationRequestRepository;
import com.example.ai_tutor.global.config.security.util.CustomCookie;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;

import static com.example.ai_tutor.domain.auth.domain.repository.CustomAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomSimpleUrlAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 로그인 실패: {}", exception.getMessage());

        String errorMessage = exception.getMessage();

        log.info("Request URI: {}", request.getRequestURI());
        log.info("Query String: {}", request.getQueryString());
        log.info("Cookies: {}", Arrays.toString(request.getCookies()));

        // 세션에서 error_redirect_url 가져오기
        String errorRedirectUrl = (String) request.getSession().getAttribute("OAUTH2_ERROR_REDIRECT_URL");
        if (errorRedirectUrl == null || errorRedirectUrl.isEmpty()) {
            errorRedirectUrl = "https://ai-tutor.ac.kr/login?error";  // 기본 오류 리디렉션 URL
        }

        // 에러를 쿼리 파라미터에 추가
        errorRedirectUrl = UriComponentsBuilder.fromUriString(errorRedirectUrl)
                .queryParam("error", errorMessage)
                .build().toUriString();

        log.info("OAuth2 로그인 실패 - 에러 메시지: {}", errorMessage);
        getRedirectStrategy().sendRedirect(request, response, errorRedirectUrl);
    }
}
