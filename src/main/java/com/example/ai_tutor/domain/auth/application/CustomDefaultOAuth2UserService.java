package com.example.ai_tutor.domain.auth.application;

import com.example.ai_tutor.domain.user.domain.Provider;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.auth.OAuth2UserInfo;
import com.example.ai_tutor.global.config.security.auth.OAuth2UserInfoFactory;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomDefaultOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception e) {
            DefaultAssert.isAuthentication(e.getMessage());
        }
        return null;
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        // OAuth2UserInfo가 null인지 확인
        if (oAuth2UserInfo == null) {
            log.error("OAuth2UserInfo 객체가 null입니다. Provider: {}", registrationId);
            throw new OAuth2AuthenticationException("OAuth2UserInfo is null for provider: " + registrationId);
        }

        // 이메일 검증
        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            log.error("OAuth2 사용자 정보에서 이메일이 비어있습니다. Provider: {}", registrationId);
            throw new OAuth2AuthenticationException("Email is not available from OAuth2 provider: " + registrationId);
        }


        // 사용자 검색 및 처리
        User user = userRepository.findByProviderIdAndProvider(oAuth2UserInfo.getId(), Provider.valueOf(registrationId))
                .map(existingUser -> updateUser(existingUser, oAuth2UserInfo))
                .orElseGet(() -> registerNewUser(oAuth2UserRequest, oAuth2UserInfo));

        log.info("OAuth2 사용자 처리 완료 - UserId: {}, Email: {}", user.getUserId(), user.getEmail());
        return UserPrincipal.Oauth2CreateUserPrincipal(user, oAuth2User.getAttributes());
    }

    private User updateUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        log.info("기존 사용자 업데이트 - UserId: {}, Email: {}", user.getUserId(), user.getEmail());
        user.updateName(oAuth2UserInfo.getName());
        return userRepository.save(user);
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        log.info("새 사용자 등록 - Provider: {}, Email: {}", oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2UserInfo.getEmail());
        User user = User.builder()
                .provider(Provider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))
                .providerId(oAuth2UserInfo.getId())
                .email(oAuth2UserInfo.getEmail())
                .name(oAuth2UserInfo.getName())
                .build();
        return userRepository.save(user);
    }
}
