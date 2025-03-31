package com.example.ai_tutor.global.config.security.auth;

import com.example.ai_tutor.global.config.security.util.CustomCookie;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2AuthorizationRequestRedirectFilter extends OAuth2AuthorizationRequestRedirectFilter {

    public CustomOAuth2AuthorizationRequestRedirectFilter(OAuth2AuthorizationRequestResolver customOAuth2AuthorizationRequestResolver) {
        super(customOAuth2AuthorizationRequestResolver);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        // 프론트엔드에서 전달된 redirect_url과 error_redirect_url 쿼리 파라미터를 처리
        String redirectUrl = request.getParameter("redirect_url");
        String errorRedirectUrl = request.getParameter("error_redirect_url");

        // 쿼리 파라미터가 있다면 쿠키에 저장
        if (redirectUrl != null) {
            CustomCookie.addCookie(response, "redirect_uri", redirectUrl, 180);
        }

        if (errorRedirectUrl != null) {
            CustomCookie.addCookie(response, "error_redirect_uri", errorRedirectUrl, 180);
        }

        // 기존 필터 체인 동작
        super.doFilterInternal(request, response, filterChain);
    }
}
