package com.example.ai_tutor.domain.user.application;

import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.domain.user.dto.HomeUserRes;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public ResponseEntity<?> getHomeUserInfo(UserPrincipal userPrincipal) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        // User user = userRepository.findById(1L).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        HomeUserRes homeUserRes = HomeUserRes.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .role(user.getRole().toString())
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(homeUserRes)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

}
