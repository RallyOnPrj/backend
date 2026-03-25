package com.gumraze.rallyon.backend.authorization.adapter.in.web;

import com.gumraze.rallyon.backend.authorization.adapter.out.session.BrowserAuthSessionRepository;
import com.gumraze.rallyon.backend.authorization.application.port.in.BrowserAuthorizationUseCase;
import com.gumraze.rallyon.backend.authorization.config.AuthorizationProperties;
import com.gumraze.rallyon.backend.authorization.domain.BrowserAuthSession;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/identity")
public class AuthorizationSessionController {

    private final BrowserAuthorizationUseCase browserAuthorizationUseCase;
    private final BrowserAuthSessionRepository browserAuthSessionRepository;
    private final AuthorizationTokenCookieService authorizationTokenCookieService;
    private final AuthenticatedIdentityContextService authenticatedIdentityContextService;
    private final AuthorizationProperties authorizationProperties;

    public AuthorizationSessionController(
            BrowserAuthorizationUseCase browserAuthorizationUseCase,
            BrowserAuthSessionRepository browserAuthSessionRepository,
            AuthorizationTokenCookieService authorizationTokenCookieService,
            AuthenticatedIdentityContextService authenticatedIdentityContextService,
            AuthorizationProperties authorizationProperties
    ) {
        this.browserAuthorizationUseCase = browserAuthorizationUseCase;
        this.browserAuthSessionRepository = browserAuthSessionRepository;
        this.authorizationTokenCookieService = authorizationTokenCookieService;
        this.authenticatedIdentityContextService = authenticatedIdentityContextService;
        this.authorizationProperties = authorizationProperties;
    }

    @GetMapping("/sessions/current")
    public ResponseEntity<BrowserAuthorizationUseCase.CurrentSessionView> currentSession(HttpServletRequest request) {
        return ResponseEntity.ok(
                browserAuthorizationUseCase.getCurrentSession(loadAuthSession(request))
        );
    }

    @PostMapping("/sessions")
    public ResponseEntity<CreateSessionResponse> createSession(
            @RequestBody(required = false) CreateSessionRequest requestBody,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        CreateSessionRequest requestValue =
                requestBody == null ? new CreateSessionRequest(null, null, null, null) : requestBody;

        AuthenticatedIdentity currentIdentity = authenticatedIdentityContextService.resolve(
                SecurityContextHolder.getContext().getAuthentication()
        );

        BrowserAuthorizationUseCase.SessionStartResult result = browserAuthorizationUseCase.startSession(
                new BrowserAuthorizationUseCase.StartSessionCommand(
                        requestValue.provider(),
                        requestValue.dummyCode(),
                        requestValue.screen(),
                        requestValue.returnTo(),
                        currentIdentity
                )
        );

        browserAuthSessionRepository.save(request.getSession(true), result.authSession());
        persistAuthenticatedIdentity(result.authenticatedIdentity(), request, response);

        return ResponseEntity.ok(new CreateSessionResponse(result.nextUrl()));
    }

    @PostMapping(path = "/sessions/local", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Void> loginWithLocalForm(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false, defaultValue = "/profile") String returnTo,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        BrowserAuthorizationUseCase.AuthorizationStepResult result =
                browserAuthorizationUseCase.loginWithLocalSession(
                        new BrowserAuthorizationUseCase.LocalLoginCommand(
                                email,
                                password,
                                returnTo,
                                loadAuthSession(request)
                        )
                );

        persistAuthenticatedIdentity(result.authenticatedIdentity(), request, response);
        return redirect(result.redirectLocation());
    }

    @PostMapping(path = "/registrations/local", consumes = "application/x-www-form-urlencoded")
    public ResponseEntity<Void> registerWithLocalForm(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String passwordConfirm,
            @RequestParam(required = false, defaultValue = "/profile") String returnTo,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        BrowserAuthorizationUseCase.AuthorizationStepResult result =
                browserAuthorizationUseCase.registerWithLocalSession(
                        new BrowserAuthorizationUseCase.LocalRegistrationCommand(
                                email,
                                password,
                                passwordConfirm,
                                returnTo,
                                loadAuthSession(request)
                        )
                );

        persistAuthenticatedIdentity(result.authenticatedIdentity(), request, response);
        return redirect(result.redirectLocation());
    }

    @GetMapping("/session/callback")
    public ResponseEntity<Void> sessionCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request
    ) {
        AuthenticatedIdentity currentIdentity = authenticatedIdentityContextService.resolve(
                SecurityContextHolder.getContext().getAuthentication()
        );

        BrowserAuthorizationUseCase.SessionCallbackResult result =
                browserAuthorizationUseCase.handleSessionCallback(
                        new BrowserAuthorizationUseCase.SessionCallbackCommand(
                                code,
                                state,
                                error,
                                loadAuthSession(request),
                                currentIdentity
                        )
                );

        if (result.clearSession()) {
            clearAuthSession(request.getSession(false));
        }

        if (!result.completed()) {
            return redirect(result.redirectLocation());
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .headers(headers -> {
                    headers.add(
                            HttpHeaders.SET_COOKIE,
                            authorizationTokenCookieService.buildAccessTokenCookie(result.tokenResponse().accessToken()).toString()
                    );
                    if (result.tokenResponse().refreshToken() != null && !result.tokenResponse().refreshToken().isBlank()) {
                        headers.add(
                                HttpHeaders.SET_COOKIE,
                                authorizationTokenCookieService.buildRefreshTokenCookie(result.tokenResponse().refreshToken()).toString()
                        );
                    }
                    headers.setLocation(URI.create(result.redirectLocation()));
                })
                .build();
    }

    @PostMapping("/tokens/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request) {
        BrowserAuthorizationUseCase.TokenRefreshResult result =
                browserAuthorizationUseCase.refresh(
                        findCookieValue(request, authorizationProperties.getCookies().getRefreshTokenName())
                );

        return ResponseEntity.noContent()
                .headers(headers -> {
                    headers.add(
                            HttpHeaders.SET_COOKIE,
                            authorizationTokenCookieService.buildAccessTokenCookie(result.accessToken()).toString()
                    );
                    headers.add(
                            HttpHeaders.SET_COOKIE,
                            authorizationTokenCookieService.buildRefreshTokenCookie(result.refreshToken()).toString()
                    );
                })
                .build();
    }

    @DeleteMapping("/sessions/current")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        browserAuthorizationUseCase.logout(
                findCookieValue(request, authorizationProperties.getCookies().getRefreshTokenName())
        );

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent()
                .headers(headers -> {
                    headers.add(HttpHeaders.SET_COOKIE, authorizationTokenCookieService.expireAccessTokenCookie().toString());
                    headers.add(HttpHeaders.SET_COOKIE, authorizationTokenCookieService.expireRefreshTokenCookie().toString());
                })
                .build();
    }

    private BrowserAuthSession loadAuthSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return browserAuthSessionRepository.load(session).orElse(null);
    }

    private void clearAuthSession(HttpSession session) {
        if (session != null) {
            browserAuthSessionRepository.clear(session);
        }
    }

    private void persistAuthenticatedIdentity(
            AuthenticatedIdentity authenticatedIdentity,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (authenticatedIdentity != null) {
            authenticatedIdentityContextService.save(authenticatedIdentity, request, response);
        }
    }

    private String findCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseEntity<Void> redirect(String location) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(location))
                .build();
    }

    public record CreateSessionRequest(
            AuthProvider provider,
            String dummyCode,
            String screen,
            String returnTo
    ) {
    }

    public record CreateSessionResponse(String nextUrl) {
    }
}
