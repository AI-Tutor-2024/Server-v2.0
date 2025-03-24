package com.example.ai_tutor.domain.auth.application;

import com.example.ai_tutor.domain.user.domain.Provider;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.auth.OAuth2UserInfo;
import com.example.ai_tutor.global.config.security.auth.OAuth2UserInfoFactory;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomDefaultOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        log.info("OAuth2 user loaded: {}", oAuth2User);
        try {
            log.info("Processing OAuth2 user: {}", oAuth2User);
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception e) {
            log.info("Error processing OAuth2 user: {}", e.getMessage());
            DefaultAssert.isAuthentication(e.getMessage());
        }
        return null;
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        DefaultAssert.isAuthentication(!oAuth2UserInfo.getEmail().isEmpty());

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            DefaultAssert.isAuthentication(user.getProvider().equals(Provider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId())));
            user = updateExistingUser(user, oAuth2UserInfo);
            log.info("User updated: {}", user);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
            log.info("New user registered: {}", user);
        }

        log.debug("OAuth2 user processed: {}", user);
        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        User user = User.builder()
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .password(passwordEncoder.encode(oAuth2UserInfo.getId()))
                .provider(Provider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))
                .providerId(oAuth2UserInfo.getId())
                .build();

        User savedUser = userRepository.save(user);
        log.debug("New user registered: {}", savedUser);
        return savedUser;
    }

    private User updateExistingUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        user.updateName(oAuth2UserInfo.getName());
        return userRepository.save(user);
    }

//    private String encodePassword(String password) {
//        // PasswordEncoder를 사용하여 비밀번호 인코딩
//        return passwordEncoder.encode(password);
//    }

    // 중복된 빈 등록으로 인한 오류 발생 -> 주석 처리
//    // PasswordEncoder를 Bean으로 등록하여 사용할 수 있도록 설정
//    @Bean
//    public PasswordEncoder customPasswordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

}
