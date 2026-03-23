package com.gumraze.rallyon.backend.identity.adapter.in.web;

import com.gumraze.rallyon.backend.identity.application.port.in.RegisterLocalIdentityUseCase;
import com.gumraze.rallyon.backend.identity.authorizationserver.config.AuthOriginSecurityConfig;
import com.gumraze.rallyon.backend.identity.authorizationserver.adapter.out.AuthorizationServerTokenClient;
import com.gumraze.rallyon.backend.identity.authorizationserver.config.AuthorizationServerProperties;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.BrowserAuthorizationRequestContext;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.IdentityAuthenticatedPrincipal;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.OAuthTokenResponse;
import com.gumraze.rallyon.backend.identity.authorizationserver.support.BrowserAuthorizationRequestContextRepository;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.identity.authentication.adapter.out.oauth.OAuthAuthorizationUrlFactory;
import com.gumraze.rallyon.backend.identity.authentication.application.service.LocalIdentityAuthenticator;
import com.gumraze.rallyon.backend.identity.authentication.application.service.OAuthIdentityAuthenticator;
import com.gumraze.rallyon.backend.identity.domain.authentication.AuthProvider;
import com.gumraze.rallyon.backend.identity.dto.RegisterLocalIdentityRequest;
import com.gumraze.rallyon.backend.security.config.SecurityConfig;
import com.gumraze.rallyon.backend.user.constants.UserRole;
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
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IdentityController.class)
@Import({
        SecurityConfig.class,
        AuthOriginSecurityConfig.class,
        BrowserAuthorizationRequestContextRepository.class,
        IdentityControllerTest.TestConfig.class
})
class IdentityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BrowserAuthorizationRequestContextRepository contextRepository;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private RegisterLocalIdentityUseCase registerLocalIdentityUseCase;

    @MockitoBean
    private LocalIdentityAuthenticator localIdentityAuthenticator;

    @MockitoBean
    private OAuthIdentityAuthenticator oAuthIdentityAuthenticator;

    @MockitoBean
    private OAuthAuthorizationUrlFactory oAuthAuthorizationUrlFactory;

    @MockitoBean
    private AuthorizationServerTokenClient authorizationServerTokenClient;

    @MockitoBean
    private OAuth2AuthorizationService authorizationService;

    @Test
    @DisplayName("auth 호스트에서는 프론트 로그인 UI용 컨텍스트를 제공한다")
    void login_context_is_available_on_auth_host() throws Exception {
        mockMvc.perform(get("/identity/login/context")
                        .header(HttpHeaders.HOST, "auth.rallyon.test"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("api 호스트에서는 로그인 페이지를 노출하지 않는다")
    void login_page_is_blocked_on_api_host() throws Exception {
        mockMvc.perform(get("/login")
                        .header(HttpHeaders.HOST, "api.rallyon.test"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로컬 회원가입은 계정만 생성하고 토큰 쿠키를 발급하지 않는다")
    void register_creates_account_without_issuing_cookies() throws Exception {
        RegisterLocalIdentityRequest request = RegisterLocalIdentityRequest.builder()
                .email("user@rallyon.local")
                .password("password123!")
                .build();

        MvcResult result = mockMvc.perform(post("/identity/password/register")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andReturn();

        verify(registerLocalIdentityUseCase).register(any());
        assertThat(result.getResponse().getHeaders(HttpHeaders.SET_COOKIE)).isEmpty();
    }

    @Test
    @DisplayName("세션 시작 요청은 브라우저 인증 컨텍스트를 저장하고 로그인 페이지로 리다이렉트한다")
    void start_session_redirects_to_login_when_provider_missing() throws Exception {
        MvcResult result = mockMvc.perform(get("/identity/session/start")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .param("returnTo", "/court-manager"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "/login"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(contextRepository.load(session))
                .isPresent()
                .get()
                .extracting(BrowserAuthorizationRequestContext::returnTo)
                .isEqualTo("/court-manager");
    }

    @Test
    @DisplayName("로그인 컨텍스트는 세션 상태와 허용된 로그인 수단을 반환한다")
    void login_context_returns_session_state_and_allowed_providers() throws Exception {
        MockHttpSession session = new MockHttpSession();
        contextRepository.save(session, new BrowserAuthorizationRequestContext(
                "auth-state",
                "social-state",
                "code-verifier",
                "/court-manager"
        ));

        mockMvc.perform(get("/identity/login/context")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasSession").value(true))
                .andExpect(jsonPath("$.returnTo").value("/court-manager"))
                .andExpect(jsonPath("$.allowedProviders").isArray())
                .andExpect(jsonPath("$.allowedProviders[0]").value("KAKAO"))
                .andExpect(jsonPath("$.dummyOptions").isArray());
    }

    @Test
    @DisplayName("로컬 로그인은 인증 후 표준 authorize 엔드포인트로 리다이렉트한다")
    void local_login_redirects_to_authorize_endpoint() throws Exception {
        MockHttpSession session = new MockHttpSession();
        contextRepository.save(session, new BrowserAuthorizationRequestContext(
                "auth-state",
                "social-state",
                "code-verifier",
                "/court-manager"
        ));
        when(localIdentityAuthenticator.authenticate("user@rallyon.local", "password123!"))
                .thenReturn(activePrincipal());

        mockMvc.perform(post("/identity/local/login")
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
    @DisplayName("세션 콜백은 인가 코드를 토큰으로 교환하고 HttpOnly 쿠키를 발급한다")
    void session_callback_exchanges_code_and_sets_cookies() throws Exception {
        MockHttpSession session = new MockHttpSession();
        contextRepository.save(session, new BrowserAuthorizationRequestContext(
                "auth-state",
                "social-state",
                "code-verifier",
                "/court-manager"
        ));
        when(authorizationServerTokenClient.exchangeAuthorizationCode(eq("authorization-code"), any()))
                .thenReturn(new OAuthTokenResponse(
                        "access-token",
                        "refresh-token",
                        "Bearer",
                        900L,
                        "openid profile email",
                        null
                ));

        IdentityAuthenticatedPrincipal principal = activePrincipal();

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
    @DisplayName("리프레시는 SAS 토큰 엔드포인트를 통해 access/refresh 쿠키를 다시 발급한다")
    void refresh_reissues_token_cookies() throws Exception {
        when(authorizationServerTokenClient.refresh("old-refresh"))
                .thenReturn(new OAuthTokenResponse(
                        "new-access",
                        "new-refresh",
                        "Bearer",
                        900L,
                        "openid profile email",
                        null
                ));

        MvcResult result = mockMvc.perform(post("/identity/token/refresh")
                        .header(HttpHeaders.HOST, "auth.rallyon.test")
                        .cookie(new Cookie("refresh_token", "old-refresh")))
                .andExpect(status().isNoContent())
                .andReturn();

        List<String> setCookies = result.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertThat(setCookies).anyMatch(value -> value.contains("access_token=new-access") && value.contains("Domain=.rallyon.test"));
        assertThat(setCookies).anyMatch(value -> value.contains("refresh_token=new-refresh") && !value.contains("Domain="));
    }

    @Test
    @DisplayName("로그아웃은 SAS authorization을 제거하고 인증 쿠키를 만료시킨다")
    void logout_removes_authorization_and_expires_cookies() throws Exception {
        OAuth2Authorization authorization = mock(OAuth2Authorization.class);
        when(authorizationService.findByToken("refresh-token", OAuth2TokenType.REFRESH_TOKEN))
                .thenReturn(authorization);

        MvcResult result = mockMvc.perform(post("/identity/logout")
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

    private IdentityAuthenticatedPrincipal activePrincipal() {
        return new IdentityAuthenticatedPrincipal(
                UUID.randomUUID(),
                UserRole.USER,
                UserStatus.ACTIVE,
                "tester"
        );
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        AuthorizationServerProperties authorizationServerProperties() {
            AuthorizationServerProperties properties = new AuthorizationServerProperties();
            properties.setIssuer("https://auth.rallyon.test");
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
            OAuthAllowedProvidersProperties properties = new OAuthAllowedProvidersProperties();
            properties.setAllowedProviders(List.of(AuthProvider.KAKAO, AuthProvider.GOOGLE, AuthProvider.DUMMY));
            return properties;
        }
    }
}
