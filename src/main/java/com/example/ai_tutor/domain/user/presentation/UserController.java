package com.example.ai_tutor.domain.user.presentation;

import com.example.ai_tutor.domain.user.application.UserService;
import com.example.ai_tutor.global.config.security.token.CurrentUser;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "User API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping()
    public ResponseEntity<?> getUserInfo(@CurrentUser UserPrincipal userPrincipal) {
        return userService.getHomeUserInfo(userPrincipal);
    }
}
