package com.gumraze.rallyon.backend.authorization.application.port.in;

import com.gumraze.rallyon.backend.authorization.domain.BrowserAuthSession;
import com.gumraze.rallyon.backend.authorization.domain.TokenResponse;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedAccount;

import java.util.List;

public interface BrowserAuthorizationUseCase {

    CurrentSessionView getCurrentSession(BrowserAuthSession currentSession);

    SessionStartResult startSession(StartSessionCommand command);

    AuthorizationStepResult loginWithLocalSession(LocalLoginCommand command);

    AuthorizationStepResult registerWithLocalSession(LocalRegistrationCommand command);

    AuthorizationStepResult startOAuth(StartOAuthCommand command);

    AuthorizationStepResult handleOAuthCallback(OAuthCallbackCommand command);

    SessionCallbackResult handleSessionCallback(SessionCallbackCommand command);

    TokenRefreshResult refresh(String refreshToken);

    void logout(String refreshToken);

    record StartSessionCommand(
            AuthProvider provider,
            String dummyCode,
            String screen,
            String returnTo,
            AuthenticatedAccount currentIdentity
    ) {
    }

    record SessionStartResult(
            BrowserAuthSession authSession,
            String nextUrl,
            AuthenticatedAccount authenticatedAccount
    ) {
    }

    record CurrentSessionView(
            boolean hasSession,
            String returnTo,
            String screen,
            List<AuthProvider> allowedProviders,
            List<DummyLoginOption> dummyOptions
    ) {
    }

    record DummyLoginOption(String label, String startUrl) {
    }

    record LocalLoginCommand(
            String email,
            String password,
            String returnTo,
            BrowserAuthSession authSession
    ) {
    }

    record LocalRegistrationCommand(
            String email,
            String password,
            String passwordConfirm,
            String returnTo,
            BrowserAuthSession authSession
    ) {
    }

    record StartOAuthCommand(
            AuthProvider provider,
            String dummyCode,
            BrowserAuthSession authSession,
            String callbackPath
    ) {
    }

    record OAuthCallbackCommand(
            AuthProvider provider,
            String code,
            String state,
            String error,
            BrowserAuthSession authSession,
            String callbackPath
    ) {
    }

    record AuthorizationStepResult(
            String redirectLocation,
            AuthenticatedAccount authenticatedAccount
    ) {
    }

    record SessionCallbackCommand(
            String code,
            String state,
            String error,
            BrowserAuthSession authSession,
            AuthenticatedAccount currentIdentity
    ) {
    }

    record SessionCallbackResult(
            boolean completed,
            String redirectLocation,
            TokenResponse tokenResponse,
            boolean clearSession
    ) {
        public static SessionCallbackResult redirectOnly(String redirectLocation, boolean clearSession) {
            return new SessionCallbackResult(false, redirectLocation, null, clearSession);
        }

        public static SessionCallbackResult completed(String redirectLocation, TokenResponse tokenResponse, boolean clearSession) {
            return new SessionCallbackResult(true, redirectLocation, tokenResponse, clearSession);
        }
    }

    record TokenRefreshResult(String accessToken, String refreshToken) {
    }
}
