package com.gumraze.rallyon.backend.authorization.application.service;

import com.gumraze.rallyon.backend.authorization.adapter.out.AuthorizationTokenClient;
import com.gumraze.rallyon.backend.authorization.application.port.in.BrowserAuthorizationUseCase;
import com.gumraze.rallyon.backend.authorization.config.AuthorizationProperties;
import com.gumraze.rallyon.backend.authorization.domain.BrowserAuthSession;
import com.gumraze.rallyon.backend.authorization.domain.PkceUtils;
import com.gumraze.rallyon.backend.authorization.domain.TokenResponse;
import com.gumraze.rallyon.backend.common.exception.ConflictException;
import com.gumraze.rallyon.backend.common.exception.UnauthorizedException;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAuthorizationUrlFactory;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthProviderRegistry;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.dummy.DummyOAuthProperties;
import com.gumraze.rallyon.backend.identity.application.port.in.AuthenticateLocalIdentityUseCase;
import com.gumraze.rallyon.backend.identity.application.port.in.AuthenticateOAuthIdentityUseCase;
import com.gumraze.rallyon.backend.identity.application.port.in.RegisterLocalIdentityUseCase;
import com.gumraze.rallyon.backend.identity.application.port.in.command.RegisterLocalIdentityCommand;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedAccount;
import com.gumraze.rallyon.backend.user.application.port.in.LoadUserOnboardingStatusUseCase;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class BrowserAuthorizationService implements BrowserAuthorizationUseCase {

    public static final String LOGIN_SCREEN = "login";
    public static final String SIGNUP_SCREEN = "signup";

    private final RegisterLocalIdentityUseCase registerLocalIdentityUseCase;
    private final AuthenticateLocalIdentityUseCase authenticateLocalIdentityUseCase;
    private final AuthenticateOAuthIdentityUseCase authenticateOAuthIdentityUseCase;
    private final OAuthAuthorizationUrlFactory oAuthAuthorizationUrlFactory;
    private final AuthorizationTokenClient authorizationTokenClient;
    private final AuthorizationProperties properties;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuthAllowedProvidersProperties allowedProviders;
    private final OAuthProviderRegistry oAuthProviderRegistry;
    private final DummyOAuthProperties dummyOAuthProperties;
    private final LoadUserOnboardingStatusUseCase loadUserOnboardingStatusUseCase;

    @Override
    public CurrentSessionView getCurrentSession(BrowserAuthSession currentSession) {
        return new CurrentSessionView(
                currentSession != null,
                currentSession == null ? "/profile" : currentSession.returnTo(),
                currentSession == null ? null : currentSession.screen(),
                allowedProviders.allowedProviders().stream()
                        .filter(this::isVisibleSocialProvider)
                        .toList(),
                buildDummyOptions(currentSession)
        );
    }

    @Override
    public SessionStartResult startSession(StartSessionCommand command) {
        String normalizedScreen = normalizeScreen(command.screen());
        BrowserAuthSession authSession = new BrowserAuthSession(
                PkceUtils.generateState(),
                PkceUtils.generateState(),
                PkceUtils.generateVerifier(),
                normalizeReturnTo(command.returnTo()),
                normalizedScreen
        );

        if (command.currentIdentity() != null) {
            return new SessionStartResult(authSession, buildAuthorizationUri(authSession), command.currentIdentity());
        }

        if (command.provider() == null) {
            return new SessionStartResult(authSession, buildAuthPageUri(normalizedScreen, authSession.returnTo()), null);
        }

        validateRequestedProvider(command.provider());

        if (command.provider() == AuthProvider.DUMMY) {
            if (command.dummyCode() == null || command.dummyCode().isBlank()) {
                throw new UnauthorizedException("DUMMY 로그인 코드는 필수입니다.");
            }
            AuthenticatedAccount authenticatedAccount = authenticateOAuthIdentityUseCase.authenticate(
                    command.provider(),
                    command.dummyCode(),
                    buildProviderCallbackUri(buildCanonicalCallbackPath(command.provider()))
            );
            return new SessionStartResult(authSession, buildAuthorizationUri(authSession), authenticatedAccount);
        }

        return new SessionStartResult(authSession, buildCanonicalOAuthStartPath(command.provider()), null);
    }

    @Override
    public AuthorizationStepResult loginWithLocalSession(LocalLoginCommand command) {
        if (command.authSession() == null) {
            return new AuthorizationStepResult(
                    authPageWithError(LOGIN_SCREEN, "login_session_expired", command.returnTo()),
                    null
            );
        }

        try {
            AuthenticatedAccount authenticatedAccount =
                    authenticateLocalIdentityUseCase.authenticate(command.email(), command.password());
            return new AuthorizationStepResult(buildAuthorizationUri(command.authSession()), authenticatedAccount);
        } catch (UnauthorizedException ex) {
            return new AuthorizationStepResult(
                    authPageWithError(command.authSession().screen(), "local_login_failed", command.authSession().returnTo()),
                    null
            );
        }
    }

    @Override
    public AuthorizationStepResult registerWithLocalSession(LocalRegistrationCommand command) {
        if (!Objects.equals(command.password(), command.passwordConfirm())) {
            return new AuthorizationStepResult(
                    authPageWithError(SIGNUP_SCREEN, "password_mismatch", command.returnTo()),
                    null
            );
        }

        if (command.authSession() == null) {
            return new AuthorizationStepResult(
                    authPageWithError(SIGNUP_SCREEN, "signup_session_expired", command.returnTo()),
                    null
            );
        }

        try {
            registerLocalIdentityUseCase.register(new RegisterLocalIdentityCommand(command.email(), command.password()));
        } catch (ConflictException ex) {
            return new AuthorizationStepResult(
                    authPageWithError(command.authSession().screen(), "duplicate_email", command.authSession().returnTo()),
                    null
            );
        } catch (IllegalArgumentException ex) {
            return new AuthorizationStepResult(
                    authPageWithError(command.authSession().screen(), "invalid_password", command.authSession().returnTo()),
                    null
            );
        }

        try {
            AuthenticatedAccount authenticatedAccount =
                    authenticateLocalIdentityUseCase.authenticate(command.email(), command.password());
            return new AuthorizationStepResult(buildAuthorizationUri(command.authSession()), authenticatedAccount);
        } catch (UnauthorizedException ex) {
            return new AuthorizationStepResult(
                    authPageWithError(command.authSession().screen(), "local_login_failed", command.authSession().returnTo()),
                    null
            );
        }
    }

    @Override
    public AuthorizationStepResult startOAuth(StartOAuthCommand command) {
        validateRequestedProvider(command.provider());

        if (command.authSession() == null) {
            throw new UnauthorizedException("로그인 세션이 만료되었습니다. 다시 시도해주세요.");
        }

        if (command.provider() == AuthProvider.DUMMY) {
            if (command.dummyCode() == null || command.dummyCode().isBlank()) {
                throw new UnauthorizedException("DUMMY 로그인 코드는 필수입니다.");
            }

            AuthenticatedAccount authenticatedAccount = authenticateOAuthIdentityUseCase.authenticate(
                    command.provider(),
                    command.dummyCode(),
                    buildProviderCallbackUri(command.callbackPath())
            );
            return new AuthorizationStepResult(buildAuthorizationUri(command.authSession()), authenticatedAccount);
        }

        String redirectUri = buildProviderCallbackUri(command.callbackPath());
        String providerAuthorizationUrl =
                oAuthAuthorizationUrlFactory.create(command.provider(), redirectUri, command.authSession().socialState());
        return new AuthorizationStepResult(providerAuthorizationUrl, null);
    }

    @Override
    public AuthorizationStepResult handleOAuthCallback(OAuthCallbackCommand command) {
        if (command.error() != null) {
            return new AuthorizationStepResult(
                    authPageWithError(
                            command.authSession() == null ? LOGIN_SCREEN : command.authSession().screen(),
                            "social_login_failed",
                            command.authSession() == null ? "/profile" : command.authSession().returnTo()
                    ),
                    null
            );
        }

        if (command.authSession() == null) {
            throw new UnauthorizedException("로그인 세션이 만료되었습니다. 다시 시도해주세요.");
        }

        if (command.code() == null
                || command.state() == null
                || !command.state().equals(command.authSession().socialState())) {
            return new AuthorizationStepResult(
                    authPageWithError(command.authSession().screen(), "invalid_social_callback", command.authSession().returnTo()),
                    null
            );
        }

        String redirectUri = buildProviderCallbackUri(command.callbackPath());
        AuthenticatedAccount authenticatedAccount = authenticateOAuthIdentityUseCase.authenticate(
                command.provider(),
                command.code(),
                redirectUri
        );
        return new AuthorizationStepResult(buildAuthorizationUri(command.authSession()), authenticatedAccount);
    }

    @Override
    public SessionCallbackResult handleSessionCallback(SessionCallbackCommand command) {
        if (command.error() != null) {
            return SessionCallbackResult.redirectOnly(
                    authPageWithError(
                            command.authSession() == null ? LOGIN_SCREEN : command.authSession().screen(),
                            "authorization_failed",
                            command.authSession() == null ? "/profile" : command.authSession().returnTo()
                    ),
                    true
            );
        }

        if (command.authSession() == null) {
            throw new UnauthorizedException("인증 세션이 만료되었습니다.");
        }

        if (command.code() == null
                || command.state() == null
                || !command.state().equals(command.authSession().authorizationState())) {
            return SessionCallbackResult.redirectOnly(
                    authPageWithError(command.authSession().screen(), "invalid_authorization_state", command.authSession().returnTo()),
                    true
            );
        }

        TokenResponse tokenResponse;
        try {
            tokenResponse = authorizationTokenClient.exchangeAuthorizationCode(command.code(), command.authSession());
        } catch (RestClientException ex) {
            log.warn("Authorization code exchange failed for returnTo={}", command.authSession().returnTo(), ex);
            return SessionCallbackResult.redirectOnly(
                    authPageWithError(command.authSession().screen(), "token_exchange_failed", command.authSession().returnTo()),
                    true
            );
        }

        String targetPath = command.currentIdentity() != null
                && loadUserOnboardingStatusUseCase.load(command.currentIdentity().accountId()) == UserStatus.PENDING
                ? "/profile/setup"
                : command.authSession().returnTo();

        return SessionCallbackResult.completed(properties.getFrontendBaseUrl() + targetPath, tokenResponse, true);
    }

    @Override
    public TokenRefreshResult refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh Token이 없습니다.");
        }

        TokenResponse tokenResponse = authorizationTokenClient.refresh(refreshToken);
        String refreshedRefreshToken = tokenResponse.refreshToken() == null || tokenResponse.refreshToken().isBlank()
                ? refreshToken
                : tokenResponse.refreshToken();
        return new TokenRefreshResult(tokenResponse.accessToken(), refreshedRefreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        OAuth2Authorization authorization = authorizationService.findByToken(refreshToken, OAuth2TokenType.REFRESH_TOKEN);
        if (authorization != null) {
            authorizationService.remove(authorization);
        }
    }

    private String buildAuthorizationUri(BrowserAuthSession authSession) {
        return UriComponentsBuilder.fromUriString(properties.getIssuer() + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.getBrowserClient().getClientId())
                .queryParam("redirect_uri", properties.getBrowserClient().getRedirectUri())
                .queryParam("scope", String.join(" ", properties.getBrowserClient().getScopes()))
                .queryParam("state", authSession.authorizationState())
                .queryParam("code_challenge", PkceUtils.toCodeChallenge(authSession.codeVerifier()))
                .queryParam("code_challenge_method", "S256")
                .encode()
                .toUriString();
    }

    private String buildAuthPageUri(String screen, String returnTo) {
        return UriComponentsBuilder.fromPath("/" + normalizeScreen(screen))
                .queryParam("returnTo", normalizeReturnTo(returnTo))
                .build(true)
                .toUriString();
    }

    private String buildCanonicalOAuthStartPath(AuthProvider provider) {
        return "/identity/oauth/" + provider.name();
    }

    private String buildCanonicalCallbackPath(AuthProvider provider) {
        return "/identity/oauth/" + provider.name() + "/callback";
    }

    private String buildProviderCallbackUri(String callbackPath) {
        return properties.getIssuer() + callbackPath;
    }

    private String normalizeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank() || !returnTo.startsWith("/")) {
            return "/profile";
        }
        return returnTo;
    }

    private String normalizeScreen(String screen) {
        return SIGNUP_SCREEN.equalsIgnoreCase(screen) ? SIGNUP_SCREEN : LOGIN_SCREEN;
    }

    private String authPageWithError(String screen, String errorCode, String returnTo) {
        return UriComponentsBuilder.fromUriString(properties.getIssuer() + "/" + normalizeScreen(screen))
                .queryParam("error", errorCode)
                .queryParam("returnTo", normalizeReturnTo(returnTo))
                .build(true)
                .toUriString();
    }

    private List<DummyLoginOption> buildDummyOptions(BrowserAuthSession authSession) {
        if (authSession == null || !isDummyLoginVisible()) {
            return List.of();
        }

        return List.of(
                new DummyLoginOption("DUMMY 로그인 (manager-local)", buildDummyStartUrl("manager-local")),
                new DummyLoginOption("DUMMY 로그인 (player-local)", buildDummyStartUrl("player-local")),
                new DummyLoginOption("DUMMY 로그인 (fresh-20ab6990)", buildDummyStartUrl("fresh-20ab6990"))
        );
    }

    private String buildDummyStartUrl(String dummyCode) {
        return UriComponentsBuilder.fromPath("/identity/oauth/" + AuthProvider.DUMMY.name())
                .queryParam("dummyCode", dummyCode)
                .build(true)
                .toUriString();
    }

    private boolean isVisibleSocialProvider(AuthProvider provider) {
        return provider != AuthProvider.DUMMY
                && allowedProviders.allowedProviders().contains(provider)
                && oAuthProviderRegistry.supports(provider);
    }

    private boolean isDummyLoginVisible() {
        return dummyOAuthProperties.enabled()
                && dummyOAuthProperties.loginPageVisible()
                && allowedProviders.allowedProviders().contains(AuthProvider.DUMMY)
                && oAuthProviderRegistry.supports(AuthProvider.DUMMY);
    }

    private void validateRequestedProvider(AuthProvider provider) {
        if (!allowedProviders.allowedProviders().contains(provider)) {
            throw new UnauthorizedException("허용되지 않는 로그인 수단입니다.");
        }

        if (provider == AuthProvider.DUMMY && !isDummyLoginVisible()) {
            throw new UnauthorizedException("테스트 로그인은 현재 환경에서 사용할 수 없습니다.");
        }

        if (provider != AuthProvider.DUMMY && !oAuthProviderRegistry.supports(provider)) {
            throw new UnauthorizedException("현재 사용할 수 없는 로그인 수단입니다.");
        }
    }
}
