package com.example.ai_tutor.domain.auth.application;

import com.example.ai_tutor.domain.auth.dto.TokenMapping;
import com.example.ai_tutor.global.config.security.OAuth2Config;
import com.example.ai_tutor.global.config.security.token.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
public class CustomTokenProviderService {

    @Autowired
    private OAuth2Config oAuth2Config;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

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

    public TokenMapping createToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

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
            log.info("JWT 검증 시작 - 토큰: {}", token);

            String secretKey = oAuth2Config.getAuth().getTokenSecret();
            if (secretKey == null || secretKey.isEmpty()) {
                log.error("JWT 서명 검증 실패 - Secret Key가 설정되지 않았음");
                return false;
            }

            log.info("JWT 서명 검증을 위한 Secret Key 사용: {}", secretKey);

            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

            log.info("JWT 검증 성공 - 유효한 토큰입니다.");
            return true;

        } catch (SecurityException | MalformedJwtException ex) {
            log.error("잘못된 JWT 서명 - JWT가 변조되었거나, 서명이 일치하지 않습니다. 원인: {}", ex.getMessage());

        } catch (ExpiredJwtException ex) {
            log.error("만료된 JWT 토큰 - 만료 시간: {} | 현재 시간: {}",
                    ex.getClaims().getExpiration(), new Date());

        } catch (UnsupportedJwtException ex) {
            log.error("지원되지 않는 JWT 형식 - 제공된 토큰이 예상된 형식과 다릅니다. 원인: {}", ex.getMessage());

        } catch (IllegalArgumentException ex) {
            log.error("JWT 값이 유효하지 않음 - 빈 값이거나, null이 전달됨. 원인: {}", ex.getMessage());

        } catch (Exception ex) {
            log.error("예기치 않은 JWT 검증 오류 발생: {}", ex.getMessage(), ex);
        }

        return false;
    }

}
