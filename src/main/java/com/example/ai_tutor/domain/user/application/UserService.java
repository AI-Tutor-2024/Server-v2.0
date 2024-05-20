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
        User user = validUserById(userPrincipal.getId());
        String day = calculateDaysSinceUserRegistration(user);

        HomeUserRes homeUserRes = HomeUserRes.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .day(day)
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(homeUserRes)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    private String calculateDaysSinceUserRegistration(User user) {
        // 사용자 가입일부터 현재 날짜까지의 기간을 계산
        Period period = Period.between(user.getCreatedAt().toLocalDate(), LocalDate.now());
        // 일수를 2자리 문자열로 포맷
        int days = period.getDays();
        return String.format("%02d", days);
    }

    private User validUserById(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        DefaultAssert.isTrue(user.isPresent(), "유저 정보가 올바르지 않습니다.");

        return user.get();
    }
}
