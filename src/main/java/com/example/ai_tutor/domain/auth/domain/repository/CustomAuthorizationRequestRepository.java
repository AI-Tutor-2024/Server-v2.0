package com.example.ai_tutor.domain.auth.domain.repository;

import com.example.ai_tutor.global.config.security.util.CustomCookie;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Slf4j
public class CustomAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";

    private static final int cookieExpireSeconds = 60*60;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Optional<Cookie> cookieOpt = CustomCookie.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);

        if (cookieOpt.isEmpty()) {
            log.warn("[loadAuthorizationRequest] 쿠키 없음: {}", OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            return null;
        }

        try {
            OAuth2AuthorizationRequest requestObj = CustomCookie.deserialize(cookieOpt.get(), OAuth2AuthorizationRequest.class);
            log.info("[loadAuthorizationRequest] 쿠키 로드 성공");
            return requestObj;
        } catch (Exception e) {
            log.error("[loadAuthorizationRequest] 쿠키 역직렬화 실패", e);
            return null;
        }
    }


    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            log.info("[saveAuthRequest] 삭제 요청");
            CustomCookie.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            CustomCookie.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }

        CustomCookie.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, CustomCookie.serialize(authorizationRequest), cookieExpireSeconds);
        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            CustomCookie.addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, cookieExpireSeconds);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CustomCookie.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CustomCookie.deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
    }

}
