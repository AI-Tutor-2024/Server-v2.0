package com.example.ai_tutor.global;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
public class JwtTestUtil {

    private static String SECRET_KEY;

    public static String generateExpiredJwtToken(Long testUserId) {
        if (testUserId == null) {
            throw new IllegalArgumentException("JWT 생성 실패 - userId 값이 비어있습니다.");
        }

        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        Key signingKey = Keys.hmacShaKeyFor(keyBytes);

        String token = Jwts.builder()
                .setSubject(Long.toString(testUserId))  // userId Subject
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // 1초 전 만료
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();

        log.info("✅ 만료된 JWT 생성 완료: {}", token);
        return token;
    }

    @Value("${app.auth.token-secret}")
    public void setSecretKey(String secretKey) {
        SECRET_KEY = secretKey;
        log.info("✅ JWTTestUtil - SECRET_KEY 초기화 완료: {}", SECRET_KEY);
    }

    public static String generateJwtToken(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("JWT 생성 실패 - userId 값이 비어있습니다.");
        }

        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        Key signingKey = Keys.hmacShaKeyFor(keyBytes);
        String subject = String.valueOf(userId);

        String token = Jwts.builder()
                .setSubject(subject)  // userId를 Subject로 설정
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1시간 후 만료
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();

        log.info("✅ JWT 생성 완료: {}", token);
        log.info("🔍 getUserIdFromToken() - subject 값: {}", subject);
        return token;
    }


}
