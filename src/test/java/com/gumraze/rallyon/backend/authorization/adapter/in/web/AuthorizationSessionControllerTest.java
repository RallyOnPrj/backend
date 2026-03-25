package com.gumraze.rallyon.backend.authorization.adapter.in.web;

import com.gumraze.rallyon.backend.authorization.adapter.out.AuthorizationTokenClient;
import com.gumraze.rallyon.backend.authorization.adapter.out.session.BrowserAuthSessionRepository;
import com.gumraze.rallyon.backend.authorization.application.service.BrowserAuthorizationService;
import com.gumraze.rallyon.backend.authorization.config.AuthorizationHostSecurityConfig;
import com.gumraze.rallyon.backend.authorization.config.AuthorizationProperties;
import com.gumraze.rallyon.backend.authorization.domain.BrowserAuthSession;
import com.gumraze.rallyon.backend.authorization.domain.TokenResponse;
import com.gumraze.rallyon.backend.common.exception.ConflictException;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAuthorizationUrlFactory;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthProviderRegistry;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.dummy.DummyOAuthProperties;
import com.gumraze.rallyon.backend.identity.application.port.in.AuthenticateLocalIdentityUseCase;
import com.gumraze.rallyon.backend.identity.application.port.in.AuthenticateOAuthIdentityUseCase;
import com.gumraze.rallyon.backend.identity.application.port.in.RegisterLocalIdentityUseCase;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;
import com.gumraze.rallyon.backend.identity.domain.IdentityRole;
import com.gumraze.rallyon.backend.security.config.SecurityConfig;
import com.gumraze.rallyon.backend.user.application.port.in.LoadUserOnboardingStatusUseCase;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AuthorizationSessionController.class,
        OAuthAuthorizationController.class
})
@Import({
        SecurityConfig.class,
        AuthorizationHostSecurityConfig.class,
        BrowserAuthSessionRepository.class,
        AuthorizationTokenCookieService.class,
        AuthenticatedIdentityContextService.class,
        BrowserAuthorizationService.class,
        AuthorizationSessionControllerTest.TestConfig.class
})
class AuthorizationSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrowserAuthSessionRepository browserAuthSessionRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private RegisterLocalIdentityUseCase registerLocalIdentityUseCase;

    @MockitoBean
    private AuthenticateLocalIdentityUseCase authenticateLocalIdentityUseCase;

    @MockitoBean
    private AuthenticateOAuthIdentityUseCase authenticateOAuthIdentityUseCase;

    @MockitoBean
    private OAuthAuthorizationUrlFactory oAuthAuthorizationUrlFactory;

    @MockitoBean
    private AuthorizationTokenClient authorizationTokenClient;

    @MockitoBean
    private OAuth2AuthorizationService authorizationService;

    @MockitoBean
    private OAuthProviderRegistry oAuthProviderRegistry;

    @MockitoBean
    private LoadUserOnboardingStatusUseCase loadUserOnboardingStatusUseCase;

    @Autowired
    void setUpRegistryDefaults() {
        lenient().when(oAuthProviderRegistry.supports(AuthProvider.KAKAO)).thenReturn(true);
        lenient().when(oAuthProviderRegistry.supports(AuthProvider.GOOGLE)).thenReturn(true);
        lenient().when(oAuthProviderRegistry.supports(AuthProvider.APPLE)).thenReturn(true);
        lenient().when(oAuthProviderRegistry.supports(AuthProvider.DUMMY)).thenReturn(true);
        lenient().when(loadUserOnboardingStatusUseCase.load(any())).thenReturn(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("현재 세션 조회는 세션 상태와 화면 정보를 반환한다")
    void current_session_returns_session_state_and_screen() throws Exception {
        MockHttpSession session = new MockHttpSession();
        browserAuthSessionRepository.save(session, new BrowserAuthSession(
                "auth-state",
                "social-state",
                "code-verifier",
                "/court-manager",
                "signup"
        ));

        mockMvc.perform(get("/identity/sessions/current")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasSession").value(true))
                .andExpect(jsonPath("$.returnTo").value("/court-manager"))
                .andExpect(jsonPath("$.screen").value("signup"))
                .andExpect(jsonPath("$.allowedProviders[0]").value("KAKAO"));
    }

    @Test
    @DisplayName("세션 생성은 auth session을 저장하고 다음 이동 URL을 반환한다")
    void create_session_returns_next_url_and_persists_context() throws Exception {
        MvcResult result = mockMvc.perform(post("/identity/sessions")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"screen":"signup","returnTo":"/profile/setup"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextUrl").value("/signup?returnTo=/profile/setup"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(browserAuthSessionRepository.load(session))
                .isPresent()
                .get()
                .extracting(BrowserAuthSession::screen, BrowserAuthSession::returnTo)
                .containsExactly("signup", "/profile/setup");
    }

    @Test
    @DisplayName("OAuth 시작은 provider authorize URL로 리다이렉트한다")
    void oauth_start_redirects_to_provider_authorization_url() throws Exception {
        MockHttpSession session = new MockHttpSession();
        browserAuthSessionRepository.save(session, new BrowserAuthSession(
                "auth-state",
                "social-state",
                "code-verifier",
                "/court-manager",
                "login"
        ));
        when(oAuthAuthorizationUrlFactory.create(
                AuthProvider.KAKAO,
                "https://auth.rallyon.test/identity/oauth/KAKAO/callback",
                "social-state"
        )).thenReturn("https://kauth.kakao.com/oauth/authorize");

        mockMvc.perform(get("/identity/oauth/KAKAO")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .session(session))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://kauth.kakao.com/oauth/authorize"));
    }

    @Test
    @DisplayName("로컬 로그인 canonical API는 인증 후 authorize 엔드포인트로 리다이렉트한다")
    void local_login_redirects_to_authorize_endpoint() throws Exception {
        MockHttpSession session = new MockHttpSession();
        browserAuthSessionRepository.save(session, new BrowserAuthSession(
                "auth-state",
                "social-state",
                "code-verifier",
                "/court-manager",
                "login"
        ));
        when(authenticateLocalIdentityUseCase.authenticate("user@rallyon.local", "password123!"))
                .thenReturn(activePrincipal());

        mockMvc.perform(post("/identity/sessions/local")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "user@rallyon.local")
                        .param("password", "password123!"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.containsString("/oauth2/authorize")))
                .andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.containsString("client_id=rallyon-web")))
                .andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.containsString("state=auth-state")));
    }

    @Test
    @DisplayName("로컬 회원가입 브라우저 플로우는 회원 생성 후 authorize 엔드포인트로 리다이렉트한다")
    void local_register_redirects_to_authorize_endpoint() throws Exception {
        MockHttpSession session = new MockHttpSession();
        browserAuthSessionRepository.save(session, new BrowserAuthSession(
                "auth-state",
                "social-state",
                "code-verifier",
                "/profile/setup",
                "signup"
        ));
        when(authenticateLocalIdentityUseCase.authenticate("user@rallyon.local", "password123!"))
                .thenReturn(activePrincipal());

        mockMvc.perform(post("/identity/registrations/local")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "user@rallyon.local")
                        .param("password", "password123!")
                        .param("passwordConfirm", "password123!")
                        .param("returnTo", "/profile/setup"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.containsString("/oauth2/authorize")))
                .andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.containsString("state=auth-state")));

        verify(registerLocalIdentityUseCase).register(any());
    }

    @Test
    @DisplayName("로컬 회원가입 중복 이메일은 signup 화면 에러 코드로 돌려보낸다")
    void local_register_redirects_to_signup_error_when_email_is_duplicate() throws Exception {
        MockHttpSession session = new MockHttpSession();
        browserAuthSessionRepository.save(session, new BrowserAuthSession(
                "auth-state",
                "social-state",
                "code-verifier",
                "/profile/setup",
                "signup"
        ));
        when(registerLocalIdentityUseCase.register(any()))
                .thenThrow(new ConflictException("이미 가입된 이메일입니다."));

        mockMvc.perform(post("/identity/registrations/local")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .session(session)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "user@rallyon.local")
                        .param("password", "password123!")
                        .param("passwordConfirm", "password123!")
                        .param("returnTo", "/profile/setup"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        "https://auth.rallyon.test/signup?error=duplicate_email&returnTo=/profile/setup"
                ));
    }

    @Test
    @DisplayName("canonical OAuth callback은 인증 후 authorize 엔드포인트로 리다이렉트한다")
    void canonical_oauth_callback_redirects_to_authorize_endpoint() throws Exception {
        MockHttpSession session = new MockHttpSession();
        browserAuthSessionRepository.save(session, new BrowserAuthSession(
                "auth-state",
                "social-state",
                "code-verifier",
                "/court-manager",
                "login"
        ));
        when(authenticateOAuthIdentityUseCase.authenticate(
                eq(AuthProvider.KAKAO),
                eq("social-code"),
                eq("https://auth.rallyon.test/identity/oauth/KAKAO/callback")
        )).thenReturn(activePrincipal());

        mockMvc.perform(get("/identity/oauth/KAKAO/callback")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .session(session)
                        .param("code", "social-code")
                        .param("state", "social-state"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, org.hamcrest.Matchers.containsString("/oauth2/authorize")));
    }

    @Test
    @DisplayName("세션 콜백은 인가 코드를 토큰으로 교환하고 HttpOnly 쿠키를 발급한다")
    void session_callback_exchanges_code_and_sets_cookies() throws Exception {
        MockHttpSession session = new MockHttpSession();
        browserAuthSessionRepository.save(session, new BrowserAuthSession(
                "auth-state",
                "social-state",
                "code-verifier",
                "/court-manager",
                "login"
        ));
        when(authorizationTokenClient.exchangeAuthorizationCode(eq("authorization-code"), any()))
                .thenReturn(new TokenResponse(
                        "access-token",
                        "refresh-token",
                        "Bearer",
                        900L,
                        "openid profile email",
                        null
                ));

        AuthenticatedIdentity principal = activePrincipal();

        MvcResult result = mockMvc.perform(get("/identity/session/callback")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .session(session)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()))
                        )))
                        .param("code", "authorization-code")
                        .param("state", "auth-state"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "https://rallyon.test/court-manager"))
                .andReturn();

        List<String> setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).hasSize(2);
        assertThat(setCookies).anyMatch(value -> value.contains("access_token=access-token") && value.contains("Domain=.rallyon.test"));
        assertThat(setCookies).anyMatch(value -> value.contains("refresh_token=refresh-token") && value.contains("Path=/identity") && !value.contains("Domain="));
        assertThat(setCookies).allMatch(value -> value.contains("HttpOnly"));
    }

    @Test
    @DisplayName("세션 콜백에서 토큰 교환이 실패하면 signup 에러 페이지로 돌려보낸다")
    void session_callback_redirects_to_signup_when_token_exchange_fails() throws Exception {
        MockHttpSession session = new MockHttpSession();
        browserAuthSessionRepository.save(session, new BrowserAuthSession(
                "auth-state",
                "social-state",
                "code-verifier",
                "/court-manager",
                "signup"
        ));
        when(authorizationTokenClient.exchangeAuthorizationCode(eq("authorization-code"), any()))
                .thenThrow(new ResourceAccessException("token endpoint unavailable"));

        AuthenticatedIdentity principal = activePrincipal();

        mockMvc.perform(get("/identity/session/callback")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .session(session)
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()))
                        )))
                        .param("code", "authorization-code")
                        .param("state", "auth-state"))
                .andExpect(status().isFound())
                .andExpect(header().string(
                        HttpHeaders.LOCATION,
                        "https://auth.rallyon.test/signup?error=token_exchange_failed&returnTo=/court-manager"
                ));

        assertThat(browserAuthSessionRepository.load(session)).isEmpty();
    }

    @Test
    @DisplayName("리프레시는 canonical 토큰 엔드포인트를 통해 access/refresh 쿠키를 다시 발급한다")
    void refresh_reissues_token_cookies() throws Exception {
        when(authorizationTokenClient.refresh("old-refresh"))
                .thenReturn(new TokenResponse(
                        "new-access",
                        "new-refresh",
                        "Bearer",
                        900L,
                        "openid profile email",
                        null
                ));

        MvcResult result = mockMvc.perform(post("/identity/tokens/refresh")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .cookie(new Cookie("refresh_token", "old-refresh")))
                .andExpect(status().isNoContent())
                .andReturn();

        List<String> setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).anyMatch(value -> value.contains("access_token=new-access") && value.contains("Domain=.rallyon.test"));
        assertThat(setCookies).anyMatch(value -> value.contains("refresh_token=new-refresh") && !value.contains("Domain="));
    }

    @Test
    @DisplayName("로그아웃은 authorization을 제거하고 인증 쿠키를 만료시킨다")
    void logout_removes_authorization_and_expires_cookies() throws Exception {
        OAuth2Authorization authorization = mock(OAuth2Authorization.class);
        when(authorizationService.findByToken("refresh-token", OAuth2TokenType.REFRESH_TOKEN))
                .thenReturn(authorization);

        MvcResult result = mockMvc.perform(delete("/identity/sessions/current")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .cookie(new Cookie("refresh_token", "refresh-token"))
                        .session(new MockHttpSession()))
                .andExpect(status().isNoContent())
                .andReturn();

        verify(authorizationService).remove(authorization);
        List<String> setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).hasSize(2);
        assertThat(setCookies).allMatch(value -> value.contains("Max-Age=0"));
    }

    private AuthenticatedIdentity activePrincipal() {
        return new AuthenticatedIdentity(
                UUID.randomUUID(),
                IdentityRole.USER,
                "tester"
        );
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        AuthorizationProperties authorizationProperties() {
            AuthorizationProperties properties = new AuthorizationProperties();
            properties.setIssuer("https://auth.rallyon.test");
            properties.setInternalBaseUrl("http://backend:8080");
            properties.setFrontendBaseUrl("https://rallyon.test");
            properties.getBrowserClient().setClientId("rallyon-web");
            properties.getBrowserClient().setRedirectUri("https://auth.rallyon.test/identity/session/callback");
            properties.getBrowserClient().setScopes(List.of("openid", "profile", "email"));
            properties.getCookies().setAccessTokenName("access_token");
            properties.getCookies().setRefreshTokenName("refresh_token");
            properties.getCookies().setAccessTokenDomain(".rallyon.test");
            properties.getCookies().setAccessTokenPath("/");
            properties.getCookies().setRefreshTokenPath("/identity");
            properties.getCookies().setSecure(true);
            properties.getCookies().setSameSite("Lax");
            properties.getTokens().setAccessTokenExpirationSeconds(900);
            properties.getTokens().setRefreshTokenExpirationSeconds(1209600);
            properties.getTokens().setAuthorizationCodeExpirationSeconds(300);
            return properties;
        }

        @Bean
        OAuthAllowedProvidersProperties oAuthAllowedProvidersProperties() {
            return new OAuthAllowedProvidersProperties(
                    List.of(AuthProvider.KAKAO, AuthProvider.GOOGLE, AuthProvider.APPLE, AuthProvider.DUMMY)
            );
        }

        @Bean
        DummyOAuthProperties dummyOAuthProperties() {
            return new DummyOAuthProperties(true, true);
        }
    }
}
