package com.example.ai_tutor.config;

import com.example.ai_tutor.domain.user.domain.Provider;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@Profile("test")
@RequiredArgsConstructor
public class InitData {
    private final UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;


    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
            User user = User.builder()
                    .name("test1")
                    .email("test1@gmail.com")
                    .password(passwordEncoder.encode("test1234"))
                    .provider(Provider.google)
                    .providerId("234587198")
                    .build();
            userRepository.save(user);
        }

    }
}