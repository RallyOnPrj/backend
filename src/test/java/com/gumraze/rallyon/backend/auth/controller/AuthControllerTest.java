package com.gumraze.rallyon.backend.auth.controller;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import com.gumraze.rallyon.backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.rallyon.backend.auth.service.AuthService;
import com.gumraze.rallyon.backend.auth.service.OAuthLoginResult;
import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.rallyon.backend.auth.token.JwtProperties;
import com.gumraze.rallyon.backend.common.exception.UnauthorizedException;
import com.gumraze.rallyon.backend.config.SecurityConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthControllerTest.TestConfig.class})
class AuthControllerTest {

    private static final String ACCESS_TOKEN_COOKIE = "access_token=";
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token=";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @Test
    @DisplayName("OAuth 로그인 성공 시 access/refresh 쿠키를 반환한다")
    void login_success_returns_tokens_and_cookie() throws Exception {
        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(AuthProvider.DUMMY)
                .authorizationCode("auth-code")
                .redirectUri("https://test.com")
                .build();

        when(authService.login(any(OAuthLoginRequestDto.class)))
                .thenReturn(new OAuthLoginResult(UUID.randomUUID(), "access-token", "refresh-token"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(this::assertIssuedTokenCookies);
    }

    @Test
    @DisplayName("리프레시 성공 시 access/refresh 쿠키를 재발급한다")
    void refresh_success_returns_tokens_and_cookie() throws Exception {
        when(authService.refresh("old-refresh"))
                .thenReturn(new OAuthLoginResult(UUID.randomUUID(), "new-access", "new-refresh"));

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", "old-refresh"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(this::assertIssuedTokenCookies);
    }

    @Test
    @DisplayName("리프레시 토큰이 없으면 400 ProblemDetail을 반환한다")
    void refresh_without_cookie_returns_bad_request() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("유효하지 않은 리프레시 토큰이면 401 ProblemDetail을 반환한다")
    void refresh_with_invalid_cookie_returns_unauthorized() throws Exception {
        when(authService.refresh("bad-refresh"))
                .thenThrow(new UnauthorizedException("유효하지 않은 Refresh Token입니다."));

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", "bad-refresh"))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("유효하지 않은 Refresh Token입니다."));
    }

    @Test
    @DisplayName("로그아웃 시 access/refresh 쿠키를 만료 처리하고 성공 응답을 반환한다")
    void logout_with_cookie_expires_token() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("refresh_token", "refresh-token"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(this::assertExpiredTokenCookies);

        verify(authService).logout("refresh-token");
    }

    @Test
    @DisplayName("로그아웃 시 토큰이 없으면 logout 호출 없이 성공 응답을 반환한다")
    void logout_without_cookie_returns_success() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""))
                .andExpect(this::assertExpiredTokenCookies);

        verify(authService, never()).logout(any());
    }

    /**
     * 로그인/리프레시 응답의 Set-Cookie 헤더가 access/refresh 토큰 발급 규칙을 만족하는지 검증한다.
     *
     * <p>검증 항목: 쿠키 2개 존재, HttpOnly/Secure/SameSite, Path(/, /auth) 포함</p>
     */
    private void assertIssuedTokenCookies(MvcResult result) {
        List<String> setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);

        assertThat(setCookies).hasSize(2);
        assertThat(setCookies).anyMatch(value -> value.contains(ACCESS_TOKEN_COOKIE));
        assertThat(setCookies).anyMatch(value -> value.contains(REFRESH_TOKEN_COOKIE));
        assertThat(setCookies).allMatch(value -> value.contains("HttpOnly"));
        assertThat(setCookies).allMatch(value -> value.contains("Secure"));
        assertThat(setCookies).allMatch(value -> value.contains("SameSite=Strict"));
        assertThat(setCookies).anyMatch(value -> value.contains("Path=/"));
        assertThat(setCookies).anyMatch(value -> value.contains("Path=/auth"));
    }

    /**
     * 로그아웃 응답의 Set-Cookie 헤더가 access/refresh 토큰 만료 규칙을 만족하는지 검증한다.
     *
     * <p>검증 항목: 쿠키 2개 존재, Max-Age=0, HttpOnly/Secure, Path(/, /auth) 포함</p>
     */
    private void assertExpiredTokenCookies(MvcResult result) {
        List<String> setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);

        assertThat(setCookies).hasSize(2);
        assertThat(setCookies).anyMatch(value -> value.contains(ACCESS_TOKEN_COOKIE));
        assertThat(setCookies).anyMatch(value -> value.contains(REFRESH_TOKEN_COOKIE));
        assertThat(setCookies).allMatch(value -> value.contains("Max-Age=0"));
        assertThat(setCookies).allMatch(value -> value.contains("HttpOnly"));
        assertThat(setCookies).allMatch(value -> value.contains("Secure"));
        assertThat(setCookies).anyMatch(value -> value.contains("Path=/"));
        assertThat(setCookies).anyMatch(value -> value.contains("Path=/auth"));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        JwtProperties jwtProperties() {
            return new JwtProperties(
                    new JwtProperties.AccessToken("test-secret", 3600L),
                    new JwtProperties.RefreshToken(12L)
            );
        }
    }
}
