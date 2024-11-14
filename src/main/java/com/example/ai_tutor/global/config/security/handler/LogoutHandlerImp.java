package com.example.ai_tutor.global.config.security.handler;

import com.example.ai_tutor.global.config.security.token.TokenBlacklistService;
import com.example.ai_tutor.global.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogoutHandlerImp implements LogoutHandler {

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        try {
            String jwt = getJwtFromRequest(request);
            tokenBlacklistService.addToBlacklist(jwt);

        } catch (Exception e) {
            throw new IllegalArgumentException("로그아웃에 알 수 없는 이유로 실패했습니다.");
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다. ");
        }

        return bearerToken.substring(7);
    }
}
