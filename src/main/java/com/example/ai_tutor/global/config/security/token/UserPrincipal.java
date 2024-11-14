package com.example.ai_tutor.global.config.security.token;

import com.example.ai_tutor.domain.user.domain.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@Getter
@Slf4j
public class UserPrincipal implements OAuth2User, UserDetails {

    private final Long id;
    private final UUID uuid;
    private final String email;
    private final String password;
    private final String providerId;
    private final String registrationId;
    private final Collection<? extends GrantedAuthority> authorities;

    @Setter
    private Map<String, Object> attributes;

    public UserPrincipal(Long id, UUID uuid, String email, String password, String providerId, String registrationId, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.uuid = uuid;
        this.email = email;
        this.password = password;
        this.providerId = providerId;
        this.registrationId = registrationId;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().toString())
        );

        return new UserPrincipal(
                user.getUserId(),
                user.getUserUUID(),
                user.getEmail(),
                user.getPassword(),
                user.getProviderId(),
                user.getProvider().toString(),
                authorities
        );
    }

    public static UserPrincipal Oauth2CreateUserPrincipal(User user, Map<String, Object> attributes) {
        // 입력값 검증
        if (user == null) {
            throw new IllegalArgumentException("User 객체는 null일 수 없습니다.");
        }

        Collection<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().toString())
        );

        // UserPrincipal 생성
        UserPrincipal userPrincipal = new UserPrincipal(
                user.getUserId(),
                user.getUserUUID(),
                user.getEmail(),
                null, // OAuth2 사용자는 비밀번호가 필요 없음
                user.getProviderId(),
                user.getProvider().toString(),
                authorities
        );

        // attributes 설정
        if (attributes != null && !attributes.isEmpty()) {
            userPrincipal.setAttributes(attributes);
        }

        log.info("OAuth2 UserPrincipal 생성 완료 - UserId: {}, Email: {}", user.getUserId(), user.getEmail());
        return userPrincipal;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
