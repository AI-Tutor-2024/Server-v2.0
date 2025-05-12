package com.example.ai_tutor.domain.auth.application;

import com.example.ai_tutor.domain.auth.domain.Token;
import com.example.ai_tutor.domain.auth.dto.*;
import com.example.ai_tutor.domain.auth.exception.InvalidTokenException;
import com.example.ai_tutor.domain.auth.domain.repository.TokenRepository;
import com.example.ai_tutor.domain.professor.domain.repository.ProfessorRepository;
import com.example.ai_tutor.domain.user.domain.Provider;
import com.example.ai_tutor.domain.user.domain.User;
import com.example.ai_tutor.domain.user.domain.repository.UserRepository;
import com.example.ai_tutor.global.DefaultAssert;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import com.example.ai_tutor.global.payload.ApiResponse;
import com.example.ai_tutor.global.payload.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

    private final CustomTokenProviderService customTokenProviderService;
    private final AuthenticationManager authenticationManager;
    private final IdTokenVerifier idTokenVerifier;
    private final UserDetailsService userDetailsService;

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final ProfessorRepository professorRepository;

    @Transactional
    public ResponseEntity<?> refresh(RefreshTokenReq tokenRefreshRequest){
        //1차 검증
        String refreshToken = tokenRefreshRequest.getRefreshToken().replace("Bearer ", "").trim();
        log.info("refreshToken : {}", refreshToken);

        boolean checkValid = valid(refreshToken);
        DefaultAssert.isAuthentication(checkValid);
        log.info("refresh token 검증 성공");

        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(InvalidTokenException::new);

        Authentication authentication = customTokenProviderService.getAuthenticationByToken(refreshToken);

        //시간 유효성 확인
        TokenMapping tokenMapping;

        Long expirationTime = customTokenProviderService.getExpiration(tokenRefreshRequest.getRefreshToken());
        if(expirationTime > 0){
            tokenMapping = customTokenProviderService.refreshToken(authentication, token.getRefreshToken());
            log.info("refresh token 갱신 성공");
        }else{
            tokenMapping = customTokenProviderService.createToken(authentication);
            log.info("refresh token 갱신 실패");
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
        Authentication authentication = customTokenProviderService.getAuthenticationByToken(refreshToken);
        DefaultAssert.isTrue(token.get().getUserEmail().equals(authentication.getName()), "사용자 인증에 실패하였습니다.");

        return true;
    }


    @Transactional
    public ResponseEntity<?> signIn(SignInReq signInReq, @RequestHeader("Authorization") String authorizationHeader) {
        // 1. 토큰 파싱
        String googleAccessToken = authorizationHeader.replace("Bearer ", "").trim();

        log.info("signInReq : {}", signInReq);

        UserInfo userInfo = idTokenVerifier.verifyIdToken(googleAccessToken, signInReq.getEmail());

        // 2. ID 토큰 검증 및 사용자 정보 추출
        if (userInfo == null) {
            throw new RuntimeException("유효하지 않은 ID 토큰");
        }
        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(userInfo.getEmail())
                            .name(userInfo.getName())
                            .password("oauth-only")
                            .provider(Provider.google)
                            .providerId(signInReq.getProviderId())
                            .build();
                    return userRepository.save(newUser);
                });

        // 4. Spring Security 인증 객체 생성
        // 인증 객체 생성 및 SecurityContext 등록
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 5. JWT 토큰 생성 및 refresh 저장
        TokenMapping tokenMapping = customTokenProviderService.createToken(authentication);

        Token token = Token.builder()
                .userEmail(user.getEmail())
                .refreshToken(tokenMapping.getRefreshToken())
                .build();
        tokenRepository.save(token);

        // 6. 응답 구성
        AuthRes authResponse = AuthRes.builder()
                .accessToken(tokenMapping.getAccessToken())
                .refreshToken(token.getRefreshToken())
                .build();

        ApiResponse apiResponse = ApiResponse.builder()
                .check(true)
                .information(authResponse)
                .build();

        return ResponseEntity.ok(apiResponse);
    }




}
