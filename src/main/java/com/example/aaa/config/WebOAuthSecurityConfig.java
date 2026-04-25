package com.example.aaa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.example.aaa.config.jwt.TokenProvider;
import com.example.aaa.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.example.aaa.config.oauth.OAuth2SuccessHandler;
import com.example.aaa.config.oauth.OAuth2UserCustomService;
import com.example.aaa.repository.RefreshTokenRepository;
import com.example.aaa.service.UserService;

import lombok.RequiredArgsConstructor;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@RequiredArgsConstructor
@Configuration
public class WebOAuthSecurityConfig {

        private final OAuth2UserCustomService oAuth2UserCustomService; // ユーザ情報処理
        private final UserService userService; // ユーザ情報処理
        private final TokenProvider tokenProvider; // トークン発行
        private final RefreshTokenRepository refreshTokenRepository;// DBにリフレッシュトークン貯蔵

        @Bean
        public WebSecurityCustomizer configure() { // 検証例外対象
                return (web) -> web.ignoring()
                        .requestMatchers(toH2Console())
                        .requestMatchers("/img/**", "/css/**", "/js/**");
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // トークン使用の場合はオフにする
                .httpBasic(httpBasic -> httpBasic.disable()) // 基本ログインとカスタムログイン使わない
                .formLogin(formLogin -> formLogin.disable()) // 基本ログインとカスタムログイン使わない
                .logout(logout -> logout.disable()) // Spring securityが提供する基本ログアウト使わない

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // サーバーがStatelessになる、ユーザーの状態を覚えない
                )

                // Security FilterのUsernamePasswordAuthenticationFilterよりJWT Filterを先に作動するようにする
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // トークンの再発給はURL無しでアクセスできるようにする、残りはAPI URL認証が必須
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/token").permitAll() // トークンの再発給はURL無しでアクセスできるようにする
                        .requestMatchers("/api/**").authenticated() // 残りはAPI URL認証が必須
                        .anyRequest().permitAll()
                )

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        // Authorization要請と関連する状態貯蔵
                        .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                                .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                        )
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuth2UserCustomService)
                        )
                        .successHandler(oAuth2SuccessHandler()) // 認証成功の場合は実行するハンドラー
                )
                // /apiで始まるurlの場合は401状態コードを返すように例外処理、権限なしのユーザーがトークン更新するように
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                )
                .build();
        }


        @Bean
        public OAuth2SuccessHandler oAuth2SuccessHandler() {
                return new OAuth2SuccessHandler(tokenProvider,
                        refreshTokenRepository,
                        oAuth2AuthorizationRequestBasedOnCookieRepository(),
                        userService
                );
        }

        @Bean
        public TokenAuthenticationFilter tokenAuthenticationFilter() {
                return new TokenAuthenticationFilter(tokenProvider);
        }

        @Bean
        public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
                return new OAuth2AuthorizationRequestBasedOnCookieRepository();
        }

        @Bean
        public BCryptPasswordEncoder bCryptPasswordEncoder() {
                return new BCryptPasswordEncoder();
        }
}