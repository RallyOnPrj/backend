package com.gumraze.rallyon.backend.identity.adapter.in.web;

import com.gumraze.rallyon.backend.common.exception.UnauthorizedException;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthProviderRegistry;
import com.gumraze.rallyon.backend.identity.adapter.out.oauth.dummy.DummyOAuthProperties;
import com.gumraze.rallyon.backend.identity.application.port.in.RegisterLocalIdentityUseCase;
import com.gumraze.rallyon.backend.identity.application.port.in.command.RegisterLocalIdentityCommand;
import com.gumraze.rallyon.backend.identity.authentication.adapter.out.oauth.OAuthAuthorizationUrlFactory;
import com.gumraze.rallyon.backend.identity.authentication.application.service.LocalIdentityAuthenticator;
import com.gumraze.rallyon.backend.identity.authentication.application.service.OAuthIdentityAuthenticator;
import com.gumraze.rallyon.backend.identity.authorizationserver.adapter.out.AuthorizationServerTokenClient;
import com.gumraze.rallyon.backend.identity.authorizationserver.config.AuthorizationServerProperties;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.BrowserAuthorizationRequestContext;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.IdentityAuthenticatedPrincipal;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.OAuthTokenResponse;
import com.gumraze.rallyon.backend.identity.authorizationserver.support.BrowserAuthorizationRequestContextRepository;
import com.gumraze.rallyon.backend.identity.authorizationserver.support.PkceUtils;
import com.gumraze.rallyon.backend.identity.domain.authentication.AuthProvider;
import com.gumraze.rallyon.backend.identity.dto.RegisterLocalIdentityRequest;
import com.gumraze.rallyon.backend.user.constants.UserRole;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/identity")
public class IdentityController {

    private final RegisterLocalIdentityUseCase registerLocalIdentityUseCase;
    private final LocalIdentityAuthenticator localIdentityAuthenticator;
    private final OAuthIdentityAuthenticator oAuthIdentityAuthenticator;
    private final OAuthAuthorizationUrlFactory oAuthAuthorizationUrlFactory;
    private final BrowserAuthorizationRequestContextRepository contextRepository;
    private final AuthorizationServerTokenClient authorizationServerTokenClient;
    private final AuthorizationServerProperties properties;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuthAllowedProvidersProperties allowedProviders;
    private final OAuthProviderRegistry oAuthProviderRegistry;
    private final DummyOAuthProperties dummyOAuthProperties;
    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public IdentityController(
            RegisterLocalIdentityUseCase registerLocalIdentityUseCase,
            LocalIdentityAuthenticator localIdentityAuthenticator,
            OAuthIdentityAuthenticator oAuthIdentityAuthenticator,
            OAuthAuthorizationUrlFactory oAuthAuthorizationUrlFactory,
            BrowserAuthorizationRequestContextRepository contextRepository,
            AuthorizationServerTokenClient authorizationServerTokenClient,
            AuthorizationServerProperties properties,
            OAuth2AuthorizationService authorizationService,
            OAuthAllowedProvidersProperties allowedProviders,
            OAuthProviderRegistry oAuthProviderRegistry,
            DummyOAuthProperties dummyOAuthProperties
    ) {
        this.registerLocalIdentityUseCase = registerLocalIdentityUseCase;
        this.localIdentityAuthenticator = localIdentityAuthenticator;
        this.oAuthIdentityAuthenticator = oAuthIdentityAuthenticator;
        this.oAuthAuthorizationUrlFactory = oAuthAuthorizationUrlFactory;
        this.contextRepository = contextRepository;
        this.authorizationServerTokenClient = authorizationServerTokenClient;
        this.properties = properties;
        this.authorizationService = authorizationService;
        this.allowedProviders = allowedProviders;
        this.oAuthProviderRegistry = oAuthProviderRegistry;
        this.dummyOAuthProperties = dummyOAuthProperties;
    }

    @PostMapping("/password/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterLocalIdentityRequest request) {
        registerLocalIdentityUseCase.register(
                new RegisterLocalIdentityCommand(request.getEmail(), request.getPassword())
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/login/context")
    public ResponseEntity<LoginContextResponse> loginContext(HttpServletRequest request) {
        Optional<BrowserAuthorizationRequestContext> context = contextRepository.load(request.getSession(true));

        return ResponseEntity.ok(new LoginContextResponse(
                context.isPresent(),
                context.map(BrowserAuthorizationRequestContext::returnTo).orElse("/profile"),
                allowedProviders.getAllowedProviders().stream()
                        .filter(this::isVisibleSocialProvider)
                        .toList(),
                buildDummyOptions(context.orElse(null))
        ));
    }

    @GetMapping("/session/start")
    public ResponseEntity<Void> startSession(
            @RequestParam(required = false) AuthProvider provider,
            @RequestParam(required = false) String dummyCode,
            @RequestParam(required = false, defaultValue = "/profile") String returnTo,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        BrowserAuthorizationRequestContext context = new BrowserAuthorizationRequestContext(
                PkceUtils.generateState(),
                PkceUtils.generateState(),
                PkceUtils.generateVerifier(),
                normalizeReturnTo(returnTo)
        );
        contextRepository.save(request.getSession(true), context);

        IdentityAuthenticatedPrincipal principal = resolveCurrentPrincipal(SecurityContextHolder.getContext().getAuthentication());
        if (principal != null) {
            saveAuthenticatedPrincipal(principal, request, response);
            return redirect(buildAuthorizationUri(context));
        }

        if (provider == null) {
            return redirect("/login");
        }

        validateRequestedProvider(provider);

        if (provider == AuthProvider.DUMMY) {
            if (dummyCode == null || dummyCode.isBlank()) {
                throw new UnauthorizedException("DUMMY 로그인 코드는 필수입니다.");
            }
            IdentityAuthenticatedPrincipal authenticatedPrincipal = oAuthIdentityAuthenticator.authenticate(
                    provider,
                    dummyCode,
                    properties.getIssuer() + "/identity/social/callback/" + provider.name()
            );
            saveAuthenticatedPrincipal(authenticatedPrincipal, request, response);
            return redirect(buildAuthorizationUri(context));
        }

        return redirect("/identity/social/start/" + provider.name());
    }

    @PostMapping("/local/login")
    public ResponseEntity<Void> loginWithLocalForm(
            @RequestParam String email,
            @RequestParam String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        IdentityAuthenticatedPrincipal principal = localIdentityAuthenticator.authenticate(email, password);
        saveAuthenticatedPrincipal(principal, request, response);

        BrowserAuthorizationRequestContext context = contextRepository.load(request.getSession())
                .orElseThrow(() -> new UnauthorizedException("로그인 세션이 만료되었습니다. 다시 시도해주세요."));

        return redirect(buildAuthorizationUri(context));
    }

    @GetMapping("/social/start/{provider}")
    public ResponseEntity<Void> startSocialLogin(
            @PathVariable AuthProvider provider,
            HttpServletRequest request
    ) {
        validateRequestedProvider(provider);

        BrowserAuthorizationRequestContext context = contextRepository.load(request.getSession())
                .orElseThrow(() -> new UnauthorizedException("로그인 세션이 만료되었습니다. 다시 시도해주세요."));

        String redirectUri = properties.getIssuer() + "/identity/social/callback/" + provider.name();
        String location = oAuthAuthorizationUrlFactory.create(provider, redirectUri, context.socialState());
        return redirect(location);
    }

    @GetMapping("/social/callback/{provider}")
    public ResponseEntity<Void> socialCallback(
            @PathVariable AuthProvider provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return handleSocialCallback(provider, code, state, error, request, response);
    }

    @PostMapping(path = "/social/callback/{provider}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> socialCallbackFormPost(
            @PathVariable AuthProvider provider,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        return handleSocialCallback(provider, code, state, error, request, response);
    }

    private ResponseEntity<Void> handleSocialCallback(
            AuthProvider provider,
            String code,
            String state,
            String error,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (error != null) {
            return redirect(loginPageWithError("social_login_failed"));
        }

        BrowserAuthorizationRequestContext context = contextRepository.load(request.getSession())
                .orElseThrow(() -> new UnauthorizedException("로그인 세션이 만료되었습니다. 다시 시도해주세요."));

        if (code == null || state == null || !state.equals(context.socialState())) {
            return redirect(loginPageWithError("invalid_social_callback"));
        }

        String redirectUri = properties.getIssuer() + "/identity/social/callback/" + provider.name();
        IdentityAuthenticatedPrincipal principal = oAuthIdentityAuthenticator.authenticate(provider, code, redirectUri);
        saveAuthenticatedPrincipal(principal, request, response);

        return redirect(buildAuthorizationUri(context));
    }

    @GetMapping("/session/callback")
    public ResponseEntity<Void> sessionCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request
    ) {
        if (error != null) {
            return redirect(loginPageWithError("authorization_failed"));
        }

        HttpSession session = request.getSession();
        BrowserAuthorizationRequestContext context = contextRepository.load(session)
                .orElseThrow(() -> new UnauthorizedException("인증 세션이 만료되었습니다."));

        if (code == null || state == null || !state.equals(context.authorizationState())) {
            contextRepository.clear(session);
            return redirect(loginPageWithError("invalid_authorization_state"));
        }

        OAuthTokenResponse tokenResponse = authorizationServerTokenClient.exchangeAuthorizationCode(code, context);
        IdentityAuthenticatedPrincipal principal = resolveCurrentPrincipal(SecurityContextHolder.getContext().getAuthentication());
        contextRepository.clear(session);

        String targetPath = principal != null && principal.status() == UserStatus.PENDING
                ? "/profile/setup"
                : context.returnTo();

        return ResponseEntity.status(HttpStatus.FOUND)
                .headers(headers -> {
                    headers.add(HttpHeaders.SET_COOKIE, buildAccessTokenCookie(tokenResponse.accessToken()).toString());
                    if (tokenResponse.refreshToken() != null && !tokenResponse.refreshToken().isBlank()) {
                        headers.add(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(tokenResponse.refreshToken()).toString());
                    }
                    headers.setLocation(URI.create(properties.getFrontendBaseUrl() + targetPath));
                })
                .build();
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<Void> refresh(
            HttpServletRequest request
    ) {
        String refreshToken = findCookieValue(request, properties.getCookies().getRefreshTokenName());
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh Token이 없습니다.");
        }

        OAuthTokenResponse tokenResponse = authorizationServerTokenClient.refresh(refreshToken);
        return ResponseEntity.noContent()
                .headers(headers -> {
                    headers.add(HttpHeaders.SET_COOKIE, buildAccessTokenCookie(tokenResponse.accessToken()).toString());
                    String refreshedRefreshToken = tokenResponse.refreshToken() == null || tokenResponse.refreshToken().isBlank()
                            ? refreshToken
                            : tokenResponse.refreshToken();
                    headers.add(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(refreshedRefreshToken).toString());
                })
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request
    ) {
        String refreshToken = findCookieValue(request, properties.getCookies().getRefreshTokenName());
        if (refreshToken != null && !refreshToken.isBlank()) {
            OAuth2Authorization authorization = authorizationService.findByToken(refreshToken, OAuth2TokenType.REFRESH_TOKEN);
            if (authorization != null) {
                authorizationService.remove(authorization);
            }
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        return ResponseEntity.noContent()
                .headers(headers -> {
                    headers.add(HttpHeaders.SET_COOKIE, expireAccessTokenCookie().toString());
                    headers.add(HttpHeaders.SET_COOKIE, expireRefreshTokenCookie().toString());
                })
                .build();
    }

    private void saveAuthenticatedPrincipal(
            IdentityAuthenticatedPrincipal principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()))
        );
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
    }

    private IdentityAuthenticatedPrincipal resolveCurrentPrincipal(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if (authentication.getPrincipal() instanceof IdentityAuthenticatedPrincipal principal) {
            return principal;
        }
        if (authentication.getCredentials() instanceof Jwt jwt) {
            UserRole role = Optional.ofNullable(jwt.getClaimAsStringList("roles"))
                    .filter(list -> !list.isEmpty())
                    .map(list -> UserRole.valueOf(list.getFirst()))
                    .orElse(UserRole.USER);
            UserStatus status = Optional.ofNullable(jwt.getClaimAsString("status"))
                    .map(UserStatus::valueOf)
                    .orElse(UserStatus.ACTIVE);
            return new IdentityAuthenticatedPrincipal(
                    java.util.UUID.fromString(jwt.getSubject()),
                    role,
                    status,
                    jwt.getClaimAsString("name")
            );
        }
        return null;
    }

    private String buildAuthorizationUri(BrowserAuthorizationRequestContext context) {
        return UriComponentsBuilder.fromUriString(properties.getIssuer() + "/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.getBrowserClient().getClientId())
                .queryParam("redirect_uri", properties.getBrowserClient().getRedirectUri())
                .queryParam("scope", String.join(" ", properties.getBrowserClient().getScopes()))
                .queryParam("state", context.authorizationState())
                .queryParam("code_challenge", PkceUtils.toCodeChallenge(context.codeVerifier()))
                .queryParam("code_challenge_method", "S256")
                .encode()
                .toUriString();
    }

    private String normalizeReturnTo(String returnTo) {
        if (returnTo == null || returnTo.isBlank() || !returnTo.startsWith("/")) {
            return "/profile";
        }
        return returnTo;
    }

    private String loginPageWithError(String errorCode) {
        return properties.getIssuer() + "/login?error=" + errorCode;
    }

    private List<DummyLoginOptionResponse> buildDummyOptions(BrowserAuthorizationRequestContext context) {
        if (context == null || !isDummyLoginVisible()) {
            return List.of();
        }

        return List.of(
                new DummyLoginOptionResponse(
                        "DUMMY 로그인 (manager-local)",
                        buildDummyStartUrl("manager-local", context.returnTo())
                ),
                new DummyLoginOptionResponse(
                        "DUMMY 로그인 (player-local)",
                        buildDummyStartUrl("player-local", context.returnTo())
                ),
                new DummyLoginOptionResponse(
                        "DUMMY 로그인 (fresh-20ab6990)",
                        buildDummyStartUrl("fresh-20ab6990", context.returnTo())
                )
        );
    }

    private String buildDummyStartUrl(String dummyCode, String returnTo) {
        return UriComponentsBuilder.fromPath("/identity/session/start")
                .queryParam("provider", AuthProvider.DUMMY.name())
                .queryParam("dummyCode", dummyCode)
                .queryParam("returnTo", returnTo)
                .build(true)
                .toUriString();
    }

    private boolean isVisibleSocialProvider(AuthProvider provider) {
        return provider != AuthProvider.DUMMY
                && allowedProviders.getAllowedProviders().contains(provider)
                && oAuthProviderRegistry.supports(provider);
    }

    private boolean isDummyLoginVisible() {
        return dummyOAuthProperties.enabled()
                && dummyOAuthProperties.loginPageVisible()
                && allowedProviders.getAllowedProviders().contains(AuthProvider.DUMMY)
                && oAuthProviderRegistry.supports(AuthProvider.DUMMY);
    }

    private void validateRequestedProvider(AuthProvider provider) {
        if (!allowedProviders.getAllowedProviders().contains(provider)) {
            throw new UnauthorizedException("허용되지 않는 로그인 수단입니다.");
        }

        if (provider == AuthProvider.DUMMY && !isDummyLoginVisible()) {
            throw new UnauthorizedException("테스트 로그인은 현재 환경에서 사용할 수 없습니다.");
        }

        if (provider != AuthProvider.DUMMY && !oAuthProviderRegistry.supports(provider)) {
            throw new UnauthorizedException("현재 사용할 수 없는 로그인 수단입니다.");
        }
    }

    private ResponseEntity<Void> redirect(String location) {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(location))
                .build();
    }

    private ResponseCookie buildAccessTokenCookie(String accessToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getCookies().getAccessTokenName(), accessToken)
                .httpOnly(true)
                .secure(properties.getCookies().isSecure())
                .path(properties.getCookies().getAccessTokenPath())
                .sameSite(properties.getCookies().getSameSite())
                .maxAge(Duration.ofSeconds(properties.getTokens().getAccessTokenExpirationSeconds()));

        if (hasText(properties.getCookies().getAccessTokenDomain())) {
            builder.domain(properties.getCookies().getAccessTokenDomain());
        }

        return builder.build();
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getCookies().getRefreshTokenName(), refreshToken)
                .httpOnly(true)
                .secure(properties.getCookies().isSecure())
                .path(properties.getCookies().getRefreshTokenPath())
                .sameSite(properties.getCookies().getSameSite())
                .maxAge(Duration.ofSeconds(properties.getTokens().getRefreshTokenExpirationSeconds()));

        if (hasText(properties.getCookies().getRefreshTokenDomain())) {
            builder.domain(properties.getCookies().getRefreshTokenDomain());
        }

        return builder.build();
    }

    private ResponseCookie expireAccessTokenCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getCookies().getAccessTokenName(), "")
                .httpOnly(true)
                .secure(properties.getCookies().isSecure())
                .path(properties.getCookies().getAccessTokenPath())
                .sameSite(properties.getCookies().getSameSite())
                .maxAge(0);

        if (hasText(properties.getCookies().getAccessTokenDomain())) {
            builder.domain(properties.getCookies().getAccessTokenDomain());
        }

        return builder.build();
    }

    private ResponseCookie expireRefreshTokenCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(properties.getCookies().getRefreshTokenName(), "")
                .httpOnly(true)
                .secure(properties.getCookies().isSecure())
                .path(properties.getCookies().getRefreshTokenPath())
                .sameSite(properties.getCookies().getSameSite())
                .maxAge(0);

        if (hasText(properties.getCookies().getRefreshTokenDomain())) {
            builder.domain(properties.getCookies().getRefreshTokenDomain());
        }

        return builder.build();
    }

    private String findCookieValue(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record LoginContextResponse(
            boolean hasSession,
            String returnTo,
            List<AuthProvider> allowedProviders,
            List<DummyLoginOptionResponse> dummyOptions
    ) {
    }

    public record DummyLoginOptionResponse(
            String label,
            String startUrl
    ) {
    }
}
