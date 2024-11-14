package com.example.ai_tutor.global.config.security.token;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    private final Key secretKey;

    /**
     * Secret Key는 JWT 토큰의 유효성 검사를 위해 사용됩니다.
     * 토큰이 블랙리스트에 추가된 후 만료되었는지 확인하려면,
     * Secret Key를 사용하여 토큰의 서명을 검증하고 만료 시간을 확인해야 합니다.
     *
     * @param key Base64로 인코딩된 시크릿 키
     */
    public TokenBlacklistService(@Value("${app.auth.token-secret}") String key) {
        byte[] byteSecretKey = Decoders.BASE64.decode(key);
        this.secretKey = Keys.hmacShaKeyFor(byteSecretKey);
        log.info("TokenBlacklistService 초기화 with secret key");
    }

    /**
     * 주어진 토큰을 블랙리스트에 추가합니다.
     *
     * @param token 블랙리스트에 추가할 JWT 토큰
     */
    public void addToBlacklist(String token) {
        blacklist.add(token);
        log.info("토큰이 블랙리스트에 추가되었습니다: {}", token);
    }

    /**
     * 주어진 토큰이 블랙리스트에 있는지 확인합니다.
     *
     * @param token 확인할 JWT 토큰
     * @return 토큰이 블랙리스트에 있는 경우 true, 그렇지 않은 경우 false
     */
    public boolean isBlacklisted(String token) {
        boolean isBlacklisted = blacklist.contains(token);
        log.debug("토큰 블랙리스트 상태 확인: {}, 블랙리스트에 있음: {}", token, isBlacklisted);
        return isBlacklisted;
    }


    /**
     * 블랙리스트에서 만료된 토큰을 주기적으로 제거합니다.
     * 이 메서드는 1시간마다 실행됩니다.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanUpExpiredTokens() {
        log.info("블랙리스트에서 만료된 토큰을 제거하는 작업 시작");
        Iterator<String> iterator = blacklist.iterator();
        while (iterator.hasNext()) {
            String token = iterator.next();
            if (isTokenExpired(token)) {
                iterator.remove();
                log.debug("만료된 토큰이 블랙리스트에서 제거되었습니다: {}", token);
            }
        }
        log.info("블랙리스트에서 만료된 토큰을 제거하는 작업 완료");
    }

    /**
     * 주어진 토큰이 만료되었는지 확인합니다.
     *
     * @param token 확인할 JWT 토큰
     * @return 토큰이 만료된 경우 true, 그렇지 않은 경우 false
     */
    private boolean isTokenExpired(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            boolean isExpired = claims.getBody().getExpiration().before(new Date());
            log.debug("토큰 만료 상태 확인: {}, 만료됨: {}", token, isExpired);
            return isExpired;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("토큰 만료 상태 확인 중 오류 발생: {}", token, e);
            return true; // 토큰이 유효하지 않으면 만료된 것으로 간주
        }
    }
}

