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

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomDefaultOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuth2UserInfoFactory oAuth2UserInfoFactory;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        log.info("loadUser Start");
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        User user = this.processOAuth2User(registrationId, attributes);
        log.info("Processing OAuth2 user: {}", oAuth2User);
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        log.info("OAuth2 로그인 UserPrincipal 생성됨 - UUID: {}", userPrincipal.getUsername());

        return userPrincipal;
        }

    private User processOAuth2User(String registrationId, Map<String, Object> attributes) {
        OAuth2UserInfo oAuth2UserInfo = oAuth2UserInfoFactory.getOAuthUserInfo(registrationId, attributes);
        DefaultAssert.isAuthentication(!oAuth2UserInfo.getEmail().isEmpty());
        Optional<User> user = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        return user.orElseGet(() -> this.registerNewUser(oAuth2UserInfo, registrationId));
    }

    private User registerNewUser(OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        User user = User.builder()
                .name(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .password("oauth-only")
                .provider(registrationId)
                .providerId(oAuth2UserInfo.getProviderId())
                .build();

        return userRepository.save(user);
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
