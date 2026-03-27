package com.gumraze.rallyon.backend.authorization.application.service;

import com.gumraze.rallyon.backend.authorization.adapter.out.AuthorizationTokenClient;
import com.gumraze.rallyon.backend.authorization.application.port.in.BrowserAuthorizationUseCase;
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
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedAccount;
import com.gumraze.rallyon.backend.identity.domain.AccountRole;
import com.gumraze.rallyon.backend.user.application.port.in.LoadUserOnboardingStatusUseCase;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrowserAuthorizationServiceTest {

    @Mock
    private RegisterLocalIdentityUseCase registerLocalIdentityUseCase;

    @Mock
    private AuthenticateLocalIdentityUseCase authenticateLocalIdentityUseCase;

    @Mock
    private AuthenticateOAuthIdentityUseCase authenticateOAuthIdentityUseCase;

    @Mock
    private OAuthAuthorizationUrlFactory oAuthAuthorizationUrlFactory;

    @Mock
    private AuthorizationTokenClient authorizationTokenClient;

    @Mock
    private org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService authorizationService;

    @Mock
    private OAuthProviderRegistry oAuthProviderRegistry;

    @Mock
    private LoadUserOnboardingStatusUseCase loadUserOnboardingStatusUseCase;

    private BrowserAuthorizationService service;

    @BeforeEach
    void setUp() {
        AuthorizationProperties properties = new AuthorizationProperties();
        properties.setIssuer("https://auth.rallyon.test");
        properties.setFrontendBaseUrl("https://rallyon.test");
        properties.getBrowserClient().setClientId("rallyon-web");
        properties.getBrowserClient().setRedirectUri("https://auth.rallyon.test/identity/session/callback");
        properties.getBrowserClient().setScopes(List.of("openid", "profile", "email"));

        service = new BrowserAuthorizationService(
                registerLocalIdentityUseCase,
                authenticateLocalIdentityUseCase,
                authenticateOAuthIdentityUseCase,
                oAuthAuthorizationUrlFactory,
                authorizationTokenClient,
                properties,
                authorizationService,
                new OAuthAllowedProvidersProperties(List.of(AuthProvider.KAKAO, AuthProvider.DUMMY)),
                oAuthProviderRegistry,
                new DummyOAuthProperties(true, true),
                loadUserOnboardingStatusUseCase
        );

        lenient().when(oAuthProviderRegistry.supports(AuthProvider.KAKAO)).thenReturn(true);
        lenient().when(oAuthProviderRegistry.supports(AuthProvider.DUMMY)).thenReturn(true);
    }

    @Test
    @DisplayName("현재 세션 조회는 허용 provider와 dummy 옵션을 함께 반환한다")
    void get_current_session_returns_provider_and_dummy_options() {
        BrowserAuthSession authSession = new BrowserAuthSession(
                "auth-state",
                "social-state",
                "code-verifier",
                "/court-manager",
                "signup"
        );

        BrowserAuthorizationUseCase.CurrentSessionView result = service.getCurrentSession(authSession);

        assertThat(result.hasSession()).isTrue();
        assertThat(result.returnTo()).isEqualTo("/court-manager");
        assertThat(result.screen()).isEqualTo("signup");
        assertThat(result.allowedProviders()).containsExactly(AuthProvider.KAKAO);
        assertThat(result.dummyOptions()).hasSize(3);
        assertThat(result.dummyOptions().getFirst().startUrl()).contains("/identity/oauth/DUMMY");
    }

    @Test
    @DisplayName("세션 시작은 화면 URL과 PKCE 세션을 생성한다")
    void start_session_generates_auth_session() {
        BrowserAuthorizationUseCase.SessionStartResult result = service.startSession(
                new BrowserAuthorizationUseCase.StartSessionCommand(null, null, "signup", "/profile/setup", null)
        );

        assertThat(result.nextUrl()).isEqualTo("/signup?returnTo=/profile/setup");
        assertThat(result.authSession().authorizationState()).isNotBlank();
        assertThat(result.authSession().socialState()).isNotBlank();
        assertThat(result.authSession().codeVerifier()).isNotBlank();
        assertThat(result.authenticatedAccount()).isNull();
    }

    @Test
    @DisplayName("로컬 로그인 성공 시 authorize URL과 principal을 반환한다")
    void login_with_local_session_returns_authorize_url() {
        BrowserAuthSession authSession = authSession("login", "/court-manager");
        AuthenticatedAccount authenticatedAccount = principal();
        given(authenticateLocalIdentityUseCase.authenticate("user@rallyon.local", "password123!"))
                .willReturn(authenticatedAccount);

        BrowserAuthorizationUseCase.AuthorizationStepResult result = service.loginWithLocalSession(
                new BrowserAuthorizationUseCase.LocalLoginCommand(
                        "user@rallyon.local",
                        "password123!",
                        "/court-manager",
                        authSession
                )
        );

        assertThat(result.redirectLocation()).contains("/oauth2/authorize");
        assertThat(result.redirectLocation()).contains("state=" + authSession.authorizationState());
        assertThat(result.redirectLocation()).contains("code_challenge_method=S256");
        assertThat(result.authenticatedAccount()).isEqualTo(authenticatedAccount);
    }

    @Test
    @DisplayName("로컬 회원가입 성공 시 회원 생성 후 authorize URL과 principal을 반환한다")
    void register_with_local_session_returns_authorize_url() {
        BrowserAuthSession authSession = authSession("signup", "/profile/setup");
        AuthenticatedAccount authenticatedAccount = principal();
        given(authenticateLocalIdentityUseCase.authenticate("user@rallyon.local", "password123!"))
                .willReturn(authenticatedAccount);

        BrowserAuthorizationUseCase.AuthorizationStepResult result = service.registerWithLocalSession(
                new BrowserAuthorizationUseCase.LocalRegistrationCommand(
                        "user@rallyon.local",
                        "password123!",
                        "password123!",
                        "/profile/setup",
                        authSession
                )
        );

        verify(registerLocalIdentityUseCase).register(any());
        assertThat(result.redirectLocation()).contains("/oauth2/authorize");
        assertThat(result.authenticatedAccount()).isEqualTo(authenticatedAccount);
    }

    @Test
    @DisplayName("OAuth callback state가 다르면 에러 화면으로 돌려보낸다")
    void handle_oauth_callback_returns_error_when_state_is_invalid() {
        BrowserAuthSession authSession = authSession("login", "/profile");

        BrowserAuthorizationUseCase.AuthorizationStepResult result = service.handleOAuthCallback(
                new BrowserAuthorizationUseCase.OAuthCallbackCommand(
                        AuthProvider.KAKAO,
                        "code",
                        "invalid-state",
                        null,
                        authSession,
                        "/identity/oauth/KAKAO/callback"
                )
        );

        assertThat(result.redirectLocation()).isEqualTo(
                "https://auth.rallyon.test/login?error=invalid_social_callback&returnTo=/profile"
        );
        assertThat(result.authenticatedAccount()).isNull();
    }

    @Test
    @DisplayName("authorization callback state가 다르면 세션을 지우고 에러 화면으로 돌려보낸다")
    void handle_session_callback_returns_error_when_state_is_invalid() {
        BrowserAuthSession authSession = authSession("login", "/profile");

        BrowserAuthorizationUseCase.SessionCallbackResult result = service.handleSessionCallback(
                new BrowserAuthorizationUseCase.SessionCallbackCommand(
                        "code",
                        "invalid-state",
                        null,
                        authSession,
                        principal()
                )
        );

        assertThat(result.completed()).isFalse();
        assertThat(result.clearSession()).isTrue();
        assertThat(result.redirectLocation()).isEqualTo(
                "https://auth.rallyon.test/login?error=invalid_authorization_state&returnTo=/profile"
        );
    }

    @Test
    @DisplayName("PENDING 사용자의 callback 완료는 /profile/setup으로 보낸다")
    void handle_session_callback_redirects_pending_user_to_profile_setup() {
        BrowserAuthSession authSession = authSession("signup", "/court-manager");
        AuthenticatedAccount authenticatedAccount = principal();
        given(authorizationTokenClient.exchangeAuthorizationCode("code", authSession))
                .willReturn(new TokenResponse("access", "refresh", "Bearer", 3600L, "openid", null));
        given(loadUserOnboardingStatusUseCase.load(authenticatedAccount.accountId())).willReturn(UserStatus.PENDING);

        BrowserAuthorizationUseCase.SessionCallbackResult result = service.handleSessionCallback(
                new BrowserAuthorizationUseCase.SessionCallbackCommand(
                        "code",
                        authSession.authorizationState(),
                        null,
                        authSession,
                        authenticatedAccount
                )
        );

        assertThat(result.completed()).isTrue();
        assertThat(result.redirectLocation()).isEqualTo("https://rallyon.test/profile/setup");
        assertThat(result.tokenResponse().accessToken()).isEqualTo("access");
    }

    @Test
    @DisplayName("refresh 응답에 refresh token이 없으면 기존 token을 유지한다")
    void refresh_reuses_existing_refresh_token_when_response_is_blank() {
        given(authorizationTokenClient.refresh("current-refresh"))
                .willReturn(new TokenResponse("new-access", "", "Bearer", 3600L, "openid", null));

        BrowserAuthorizationUseCase.TokenRefreshResult result = service.refresh("current-refresh");

        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.refreshToken()).isEqualTo("current-refresh");
    }

    @Test
    @DisplayName("logout은 refresh token으로 authorization을 찾아 revoke한다")
    void logout_removes_authorization_when_refresh_token_exists() {
        org.springframework.security.oauth2.server.authorization.OAuth2Authorization authorization =
                org.mockito.Mockito.mock(org.springframework.security.oauth2.server.authorization.OAuth2Authorization.class);
        given(authorizationService.findByToken("refresh-token", org.springframework.security.oauth2.server.authorization.OAuth2TokenType.REFRESH_TOKEN))
                .willReturn(authorization);

        service.logout("refresh-token");

        verify(authorizationService).remove(authorization);
    }

    @Test
    @DisplayName("중복 이메일 회원가입은 duplicate_email 에러 화면으로 돌려보낸다")
    void register_with_local_session_returns_duplicate_email_error() {
        BrowserAuthSession authSession = authSession("signup", "/profile/setup");
        given(registerLocalIdentityUseCase.register(any()))
                .willThrow(new ConflictException("이미 가입된 이메일입니다."));

        BrowserAuthorizationUseCase.AuthorizationStepResult result = service.registerWithLocalSession(
                new BrowserAuthorizationUseCase.LocalRegistrationCommand(
                        "user@rallyon.local",
                        "password123!",
                        "password123!",
                        "/profile/setup",
                        authSession
                )
        );

        assertThat(result.redirectLocation()).isEqualTo(
                "https://auth.rallyon.test/signup?error=duplicate_email&returnTo=/profile/setup"
        );
        verify(authenticateLocalIdentityUseCase, never()).authenticate(any(), any());
    }

    @Test
    @DisplayName("token exchange 실패는 authorization 실패 화면으로 돌려보낸다")
    void handle_session_callback_returns_token_exchange_error() {
        BrowserAuthSession authSession = authSession("login", "/profile");
        given(authorizationTokenClient.exchangeAuthorizationCode("code", authSession))
                .willThrow(new ResourceAccessException("boom"));

        BrowserAuthorizationUseCase.SessionCallbackResult result = service.handleSessionCallback(
                new BrowserAuthorizationUseCase.SessionCallbackCommand(
                        "code",
                        authSession.authorizationState(),
                        null,
                        authSession,
                        principal()
                )
        );

        assertThat(result.completed()).isFalse();
        assertThat(result.redirectLocation()).isEqualTo(
                "https://auth.rallyon.test/login?error=token_exchange_failed&returnTo=/profile"
        );
    }

    private BrowserAuthSession authSession(String screen, String returnTo) {
        return new BrowserAuthSession("auth-state", "social-state", "code-verifier", returnTo, screen);
    }

    private AuthenticatedAccount principal() {
        return new AuthenticatedAccount(UUID.randomUUID(), AccountRole.USER, "player");
    }
}
