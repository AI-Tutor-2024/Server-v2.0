package com.example.ai_tutor.domain.auth.application;

import com.example.ai_tutor.domain.auth.dto.TokenMapping;
import com.example.ai_tutor.global.config.security.OAuth2Config;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomTokenProviderService {

    public static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24;            // 1일
    public static final long TEMP_TOKEN_EXPIRE_TIME = 1000 * 60 * 15;            // 15분
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일
    private static final String USER_ROLE = "role";
    private static final String USER_EMAIL = "email_id";
    private static final String USER_UUID = "user_uuid";
    private static final String REGISTRATION_ID = "registrationId";

    private final OAuth2Config oAuth2Config;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${app.auth.token-secret}")
    private String encodedSecretKey;

    @PostConstruct
    private void init() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(encodedSecretKey);
            //    private Key secretKey;
            Key secretKey = Keys.hmacShaKeyFor(keyBytes);
//            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
            log.info("[JWT 초기화] Secret Key 설정 완료");
        } catch (IllegalArgumentException ex) {
            log.error("[JWT 초기화 실패] Secret Key 변환 중 오류 발생", ex);
            throw new IllegalStateException("Secret Key 초기화 실패", ex);
        }
    }

    public TokenMapping generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        log.info("액세스 토큰 발급 시작 - UserId: {}", userPrincipal.getId());

        Claims claims = Jwts.claims().setSubject(String.valueOf(userPrincipal.getId()));
        claims.put("user_uuid", userPrincipal.getUuid().toString());
        claims.put("email_id", userPrincipal.getEmail());
        claims.put("role", userPrincipal.getAuthorities().iterator().next().getAuthority());
        claims.put("registrationId", userPrincipal.getRegistrationId());
        Date now = new Date();

        Date accessTokenExpiresIn = new Date(now.getTime() + oAuth2Config.getAuth().getAccessTokenExpirationMsec());
        Date refreshTokenExpiresIn = new Date(now.getTime() + oAuth2Config.getAuth().getRefreshTokenExpirationMsec());

        String secretKey = oAuth2Config.getAuth().getTokenSecret();

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        String accessToken = Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        String refreshToken = Jwts.builder()
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return TokenMapping.builder()
                .userEmail(userPrincipal.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    public TokenMapping refreshToken(Authentication authentication, String refreshToken) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();

        Date accessTokenExpiresIn = new Date(now.getTime() + oAuth2Config.getAuth().getAccessTokenExpirationMsec());

        String secretKey = oAuth2Config.getAuth().getTokenSecret();
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        String accessToken = Jwts.builder()
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return TokenMapping.builder()
                .userEmail(userPrincipal.getEmail())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }



    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(oAuth2Config.getAuth().getTokenSecret())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Long.parseLong(claims.getSubject());
    }

    public UsernamePasswordAuthenticationToken getAuthenticationById(String token){
        Long userId = getUserIdFromToken(token);
        UserDetails userDetails = customUserDetailsService.loadUserById(userId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return authentication;
    }

    public UsernamePasswordAuthenticationToken getAuthenticationByEmail(String email){
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        return authentication;
    }

    public Long getExpiration(String token) {
        // accessToken 남은 유효시간
        Date expiration = Jwts.parserBuilder().setSigningKey(oAuth2Config.getAuth().getTokenSecret()).build().parseClaimsJws(token).getBody().getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        //시간 계산
        return (expiration.getTime() - now);
    }

    public boolean validateToken(String token) {
        try {
            //log.info("bearerToken = {} \n oAuth2Config.getAuth()={}", token, oAuth2Config.getAuth().getTokenSecret());
            Jwts.parserBuilder().setSigningKey(oAuth2Config.getAuth().getTokenSecret()).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException ex) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (MalformedJwtException ex) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException ex) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException ex) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException ex) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

}
