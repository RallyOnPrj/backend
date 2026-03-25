package com.gumraze.rallyon.backend.authorization.adapter.in.web;

import com.gumraze.rallyon.backend.authorization.adapter.out.session.BrowserAuthSessionRepository;
import com.gumraze.rallyon.backend.authorization.application.port.in.BrowserAuthorizationUseCase;
import com.gumraze.rallyon.backend.authorization.domain.BrowserAuthSession;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedIdentity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/identity")
public class OAuthAuthorizationController {

    private final BrowserAuthorizationUseCase browserAuthorizationUseCase;
    private final BrowserAuthSessionRepository browserAuthSessionRepository;
    private final AuthenticatedIdentityContextService authenticatedIdentityContextService;

    public OAuthAuthorizationController(
            BrowserAuthorizationUseCase browserAuthorizationUseCase,
            BrowserAuthSessionRepository browserAuthSessionRepository,
            AuthenticatedIdentityContextService authenticatedIdentityContextService
    ) {
        this.browserAuthorizationUseCase = browserAuthorizationUseCase;
        this.browserAuthSessionRepository = browserAuthSessionRepository;
        this.authenticatedIdentityContextService = authenticatedIdentityContextService;
    }

    @GetMapping("/oauth/{provider}")
    public ResponseEntity<Void> startOAuth(
            @PathVariable AuthProvider provider,
            @RequestParam(required = false) String dummyCode,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        BrowserAuthorizationUseCase.AuthorizationStepResult result =
                browserAuthorizationUseCase.startOAuth(
                        new BrowserAuthorizationUseCase.StartOAuthCommand(
                                provider,
                                dummyCode,
                                loadAuthSession(request),
                                "/identity/oauth/" + provider.name() + "/callback"
                        )
                );

        persistAuthenticatedIdentity(result.authenticatedIdentity(), request, response);
        return redirect(result.redirectLocation());
    }

    @GetMapping("/oauth/{provider}/callback")
    public ResponseEntity<Void> oauthCallback(
            @PathVariable AuthProvider provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return handleCallback(provider, code, state, error, request, response);
    }

    @PostMapping(path = "/oauth/{provider}/callback", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> oauthCallbackFormPost(
            @PathVariable AuthProvider provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return handleCallback(provider, code, state, error, request, response);
    }

    private ResponseEntity<Void> handleCallback(
            AuthProvider provider,
            String code,
            String state,
            String error,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        BrowserAuthorizationUseCase.AuthorizationStepResult result =
                browserAuthorizationUseCase.handleOAuthCallback(
                        new BrowserAuthorizationUseCase.OAuthCallbackCommand(
                                provider,
                                code,
                                state,
                                error,
                                loadAuthSession(request),
                                request.getRequestURI()
                        )
                );

        persistAuthenticatedIdentity(result.authenticatedIdentity(), request, response);
        return redirect(result.redirectLocation());
    }

    private BrowserAuthSession loadAuthSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return browserAuthSessionRepository.load(session).orElse(null);
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

    private ResponseEntity<Void> redirect(String location) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(location))
                .build();
    }
}
