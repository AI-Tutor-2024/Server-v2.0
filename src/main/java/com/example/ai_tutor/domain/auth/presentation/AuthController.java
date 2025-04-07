package com.example.ai_tutor.domain.auth.presentation;

import com.example.ai_tutor.domain.auth.application.AuthService;
import com.example.ai_tutor.domain.auth.dto.AuthRes;
import com.example.ai_tutor.domain.auth.dto.RefreshTokenReq;
import com.example.ai_tutor.domain.auth.dto.SignInReq;
import com.example.ai_tutor.global.config.security.token.CurrentUser;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ErrorResponse;
import com.example.ai_tutor.global.payload.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Authorization", description = "Authorization API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "사용자가 로그인을 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = AuthRes.class))
            }),
            @ApiResponse(responseCode = "400", description = "로그인 실패", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
            }),
    })
    @PostMapping(value = "/sign-in")
    public ResponseEntity<?> signIn(
            @Parameter(description = "SignInReq Schema를 확인해주세요.", required = true)
            @RequestBody SignInReq signInReq,

            @Parameter(hidden = true) // Swagger 문서에 안 보이게
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        return authService.signIn(signInReq, authorizationHeader);
    }


    @Operation(summary = "토큰 갱신", description = "신규 토큰 갱신을 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AuthRes.class) ) } ),
            @ApiResponse(responseCode = "400", description = "토큰 갱신 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping(value = "/refresh")
    public ResponseEntity<?> refresh(
            @Parameter(description = "Schemas의 RefreshTokenRequest를 참고해주세요.", required = true) @Valid @RequestBody RefreshTokenReq tokenRefreshRequest
    ) {
        return authService.refresh(tokenRefreshRequest);
    }

    @Operation(summary = "로그아웃", description = "사용자가 로그아웃을 수행합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class) ) } ),
            @ApiResponse(responseCode = "400", description = "로그아웃 실패", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class) ) } ),
    })
    @PostMapping(value="/sign-out")
    public ResponseEntity<?> signOut(
            @Parameter(description = "Accesstoken을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal
    ) {
        return authService.signOut(userPrincipal);
    }

}
