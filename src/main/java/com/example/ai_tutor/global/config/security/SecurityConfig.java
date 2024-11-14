package com.example.ai_tutor.global.config.security;

import com.example.ai_tutor.domain.auth.application.CustomDefaultOAuth2UserService;
import com.example.ai_tutor.domain.auth.application.CustomUserDetailsService;
import com.example.ai_tutor.domain.auth.domain.repository.CustomAuthorizationRequestRepository;
import com.example.ai_tutor.global.config.security.handler.*;
import com.example.ai_tutor.global.config.security.token.CustomAuthenticationEntryPoint;
import com.example.ai_tutor.global.config.security.token.CustomOncePerRequestFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomDefaultOAuth2UserService customOAuth2UserService;
    private final CustomSimpleUrlAuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final CustomSimpleUrlAuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final CustomAuthorizationRequestRepository customAuthorizationRequestRepository;
    private final CustomOAuth2AuthorizationRequestRedirectFilter customOAuth2AuthorizationRequestRedirectFilter;
    private final LogoutHandlerImp logoutHandlerImp;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

//        FormLoginJwtFilter formLoginJwtFilter = new FormLoginJwtFilter(jwtUtil, objectMapper);
//        formLoginJwtFilter.setAuthenticationManager(authenticationManager);
//        formLoginJwtFilter.setFilterProcessesUrl("/api/v1/auth/login"); // 커스텀 로그인

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(customOncePerRequestFilter(), LogoutFilter.class)
                .addFilterBefore(customOAuth2AuthorizationRequestRedirectFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
//                .formLogin(formLogin -> formLogin
////                        .loginProcessingUrl("/api/v1/auth/login")
////                        .successHandler(formLoginSuccessHandler)
////                        .permitAll())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))

                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .baseUri("/oauth2/authorize")
                                .authorizationRequestRepository(customAuthorizationRequestRepository))
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/oauth2/callback/**"))
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler))

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .addLogoutHandler(logoutHandlerImp)
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .permitAll())

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/error", "/favicon.ico", "/**/*.png", "/**/*.gif", "/**/*.svg", "/**/*.jpg", "/**/*.html", "/**/*.css", "/**/*.js")
                        .permitAll()
                        .requestMatchers("/swagger", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/login/**","/auth/**", "/oauth2/**", "/api/v1/student/**")
                        .permitAll()
                        .anyRequest()
                        .permitAll())
        //.authenticated())
        ;

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 모든 출처 허용
        configuration.addAllowedOriginPattern("*");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomOncePerRequestFilter customOncePerRequestFilter() {
        return new CustomOncePerRequestFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        authenticationProvider.setUserDetailsService(customUserDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


}
