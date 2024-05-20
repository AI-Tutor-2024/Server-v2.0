package com.example.ai_tutor.domain.auth.application;

import com.example.ai_tutor.domain.auth.domain.Token;
import com.example.ai_tutor.domain.auth.dto.SignInReq;
import com.example.ai_tutor.domain.auth.exception.InvalidTokenException;
import com.example.ai_tutor.domain.auth.domain.repository.TokenRepository;
import com.example.ai_tutor.domain.auth.dto.AuthRes;
import com.example.ai_tutor.domain.auth.dto.RefreshTokenReq;
import com.example.ai_tutor.domain.auth.dto.TokenMapping;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.error.DefaultException;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.example.ai_tutor.global.payload.ErrorCode;
import com.example.ai_tutor.global.payload.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final CustomTokenProviderService customTokenProviderService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> refresh(RefreshTokenReq tokenRefreshRequest){
        //1차 검증
        boolean checkValid = valid(tokenRefreshRequest.getRefreshToken());
        DefaultAssert.isAuthentication(checkValid);

        Token token = tokenRepository.findByRefreshToken(tokenRefreshRequest.getRefreshToken())
                .orElseThrow(InvalidTokenException::new);
        Authentication authentication = customTokenProviderService.getAuthenticationByEmail(token.getUserEmail());

        //refresh token 정보 값을 업데이트 한다.
        //시간 유효성 확인
        TokenMapping tokenMapping;

        Long expirationTime = customTokenProviderService.getExpiration(tokenRefreshRequest.getRefreshToken());
        if(expirationTime > 0){
            tokenMapping = customTokenProviderService.refreshToken(authentication, token.getRefreshToken());
        }else{
            tokenMapping = customTokenProviderService.createToken(authentication);
        }

        Token updateToken = token.updateRefreshToken(tokenMapping.getRefreshToken());

        AuthRes authResponse = AuthRes.builder()
                .accessToken(tokenMapping.getAccessToken())
                .refreshToken(updateToken.getRefreshToken())
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(authResponse)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @Transactional
    public ResponseEntity<?> signOut(UserPrincipal userPrincipal){
        Token token = tokenRepository.findByUserEmail(userPrincipal.getEmail())
                .orElseThrow(InvalidTokenException::new);

        tokenRepository.delete(token);

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(Message.builder().message("유저가 로그아웃 되었습니다.").build())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    private boolean valid(String refreshToken){

        //1. 토큰 형식 물리적 검증
        boolean validateCheck = customTokenProviderService.validateToken(refreshToken);
        DefaultAssert.isTrue(validateCheck, "Token 검증에 실패하였습니다.");

        //2. refresh token 값을 불러온다.
        Optional<Token> token = tokenRepository.findByRefreshToken(refreshToken);
        DefaultAssert.isTrue(token.isPresent(), "탈퇴 처리된 회원입니다.");

        //3. email 값을 통해 인증값을 불러온다
        Authentication authentication = customTokenProviderService.getAuthenticationByEmail(token.get().getUserEmail());
        DefaultAssert.isTrue(token.get().getUserEmail().equals(authentication.getName()), "사용자 인증에 실패하였습니다.");

        return true;
    }

    @Transactional
    public ResponseEntity<?> signIn(SignInReq signInReq) {
        User user = userRepository.findByEmail(signInReq.getEmail())
                .orElseThrow(() -> new DefaultException(ErrorCode.INVALID_CHECK, "유저 정보가 유효하지 않습니다."));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInReq.getEmail(),
                        signInReq.getProviderId()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        TokenMapping tokenMapping = customTokenProviderService.createToken(authentication);
        Token token = Token.builder()
                .refreshToken(tokenMapping.getRefreshToken())
                .userEmail(tokenMapping.getUserEmail())
                .build();
        tokenRepository.save(token);

        AuthRes authResponse = AuthRes.builder()
                .accessToken(tokenMapping.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(authResponse).build();

        return ResponseEntity.ok(apiResponse);
    }

}
