package com.gumraze.rallyon.backend.auth.service;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import com.gumraze.rallyon.backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.rallyon.backend.auth.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.auth.oauth.OAuthUserInfo;
import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenGenerator;
import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.rallyon.backend.auth.token.JwtProperties;
import com.gumraze.rallyon.backend.common.exception.UnauthorizedException;
import com.gumraze.rallyon.backend.user.constants.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private AuthService authService;
    private JwtAccessTokenGenerator jwtAccessTokenGenerator;
    private JwtProperties properties;
    private FakeOAuthClient fakeOAuthClient;
    private FakeUserAuthRepository userAuthRepository;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties(
                new JwtProperties.AccessToken(
                        "test-secret-key-test-secret-key-test-secret-key",
                        1_800_000L
                ),
                // 5시간
                new JwtProperties.RefreshToken(5L)
        );

        jwtAccessTokenGenerator = new JwtAccessTokenGenerator(properties);
        fakeOAuthClient = new FakeOAuthClient(defaultOAuthUserInfo("oauth-user-123"));
        FakeOAuthClientResolver oAuthClientResolver = resolverWith(AuthProvider.DUMMY, fakeOAuthClient);
        userAuthRepository = new FakeUserAuthRepository();
        refreshTokenService = new FakeRefreshTokenService();

        authService = newAuthService(
                userAuthRepository,
                refreshTokenService,
                oAuthClientResolver,
                AuthProvider.DUMMY,
                AuthProvider.KAKAO
        );
    }

    @Test
    @DisplayName("OAuth 로그인을 하면 결과 객체를 반환한다.")
    void login_returns_result_when_oauth_login() {
        // given
        OAuthLoginRequestDto request = loginRequest(AuthProvider.DUMMY);

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("로그인 결과에는 accessToken이 포함된다.")
    void login_result_contains_access_token() {
        // given
        OAuthLoginRequestDto request = loginRequest(AuthProvider.DUMMY);

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(result.accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("accessToken은 사용자의 식별자(userId)를 포함한다.")
    void access_token_contains_user_identifier() {
        // given
        OAuthLoginRequestDto request = loginRequest(AuthProvider.DUMMY);

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertAccessTokenContainsUserId(result, result.userId());
    }

    @Test
    @DisplayName("OAuth 로그인 시 사용자 식별이 먼저 수행된다.")
    void oauth_login_identifies_user() {
        // given
        OAuthLoginRequestDto request = loginRequest(AuthProvider.DUMMY);

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(result.userId()).isNotNull();
    }

    @Test
    @DisplayName("OAuth 로그인 시 OAuthClient를 호출한다.")
    void oauth_login_calls_oauth_client() {
        // given
        OAuthLoginRequestDto request = loginRequest(AuthProvider.DUMMY);

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(fakeOAuthClient.isCalled()).isTrue();
    }

    @Test
    @DisplayName("OAuth 로그인 시 authorizationCode와 redirectUri를 OAuthClient에 전달한다.")
    void oauth_login_passes_authorization_code_and_redirect_uri_to_oauth_client() {
        // given
        String authorizationCode = "auth-code-123";
        String redirectUri = "https://example.com/callback";
        OAuthLoginRequestDto request = loginRequest(AuthProvider.DUMMY, authorizationCode, redirectUri);

        // when
        authService.login(request);

        // then
        assertThat(fakeOAuthClient.getLastAuthorizationCode()).isEqualTo(authorizationCode);
        assertThat(fakeOAuthClient.getLastRedirectUri()).isEqualTo(redirectUri);
    }

    @Test
    @DisplayName("이미 가입된 사용자는 providerUserId로 기존 userId를 반환한다.")
    void returns_existing_user_id_when_user_already_registered() {
        // given
        seedExistingUser(userAuthRepository, AuthProvider.DUMMY, "oauth-user-123", 10L);
        OAuthLoginRequestDto request = loginRequest(AuthProvider.DUMMY);

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(result.userId()).isEqualTo(uuid(10));
    }

    @Test
    @DisplayName("기존 사용자 로그인 시 createPendingUser를 호출하지 않는다.")
    void does_not_create_pending_user_when_existing_user_logs_in() {
        // given
        FakeUserAuthRepository localUserIdentityPort = new FakeUserAuthRepository();
        seedExistingUser(localUserIdentityPort, AuthProvider.DUMMY, "oauth-user-123", 10L);
        AuthService localAuthService = newAuthService(
                localUserIdentityPort,
                refreshTokenService,
                resolverWith(AuthProvider.DUMMY, fakeOAuthClient),
                AuthProvider.DUMMY,
                AuthProvider.KAKAO
        );

        // when
        localAuthService.login(loginRequest(AuthProvider.DUMMY));

        // then
        assertThat(localUserIdentityPort.getCreatePendingUserCallCount()).isZero();
    }

    @Test
    @DisplayName("신규 사용자는 userId를 생성하고 user_auth에 저장한다.")
    void creates_new_user_when_not_registered_yet() {
        // given
        FakeUserAuthRepository localUserIdentityPort = new FakeUserAuthRepository();
        FakeOAuthClient localOAuthClient = new FakeOAuthClient(defaultOAuthUserInfo("oauth-user-123"));
        FakeOAuthClientResolver localResolver = resolverWith(AuthProvider.DUMMY, localOAuthClient);
        AuthService localAuthService = newAuthService(
                localUserIdentityPort,
                refreshTokenService,
                localResolver,
                AuthProvider.DUMMY,
                AuthProvider.KAKAO
        );
        OAuthLoginRequestDto request = loginRequest(AuthProvider.DUMMY);

        // when
        OAuthLoginResult result = localAuthService.login(request);

        // then
        assertThat(result.userId()).isNotNull();
        assertThat(
                localUserIdentityPort.findUserId(AuthProvider.DUMMY, "oauth-user-123")
        ).isPresent();
    }

    @Test
    @DisplayName("신규 사용자일 경우 createPendingUser를 통해 사용자 식별자를 생성한다.")
    void creates_user_id_through_create_pending_user_when_new_user() {
        // given
        FakeUserAuthRepository localUserIdentityPort = new FakeUserAuthRepository();
        FakeOAuthClientResolver localResolver = resolverWith(AuthProvider.DUMMY, fakeOAuthClient);
        AuthService localAuthService = newAuthService(
                localUserIdentityPort,
                refreshTokenService,
                localResolver,
                AuthProvider.DUMMY,
                AuthProvider.KAKAO
        );
        OAuthLoginRequestDto request = loginRequest(AuthProvider.DUMMY);

        // when
        OAuthLoginResult result = localAuthService.login(request);

        // then
        assertThat(localUserIdentityPort.getCreatePendingUserCallCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("로그인 시 refreshTokenService.rotate는 식별된 userId로 호출된다.")
    void login_calls_rotate_with_resolved_user_id() {
        // given
        FakeUserAuthRepository localUserIdentityPort = new FakeUserAuthRepository();
        seedExistingUser(localUserIdentityPort, AuthProvider.DUMMY, "oauth-user-123", 10L);
        RefreshTokenService localRefreshTokenService = mock(RefreshTokenService.class);
        UUID userId = uuid(10);
        when(localRefreshTokenService.rotate(userId)).thenReturn("refresh-10");

        AuthService localAuthService = newAuthService(
                localUserIdentityPort,
                localRefreshTokenService,
                resolverWith(AuthProvider.DUMMY, fakeOAuthClient),
                AuthProvider.DUMMY,
                AuthProvider.KAKAO
        );

        // when
        OAuthLoginResult result = localAuthService.login(loginRequest(AuthProvider.DUMMY));

        // then
        assertThat(result.refreshToken()).isEqualTo("refresh-10");
        verify(localRefreshTokenService).rotate(userId);
    }

    @Test
    @DisplayName("구글 로그인 시 실패 Test")
    void fails_DUMMY_login() {
        // given: 구글 로그인으로 요청
        OAuthLoginRequestDto requestDto = loginRequest(AuthProvider.GOOGLE);

        // when: 허용되지 않는 provider 라면 예외 발생
        assertThatThrownBy(() -> authService.login(requestDto))
                // then: 예외 발생
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 provider");
    }

    @Test
    @DisplayName("OAuth 로그인 시 user_auth에 닉네임/이메일/프로필이 저장됨")
    void save_oauth_profile_fields_on_login() {
        // given: OAuth 응답에 프로필 정보다 포함됨.
        FakeOAuthClient localOAuthClient = new FakeOAuthClient(kakaoOAuthUserInfo());
        FakeOAuthClientResolver resolver = resolverWith(AuthProvider.KAKAO, localOAuthClient);
        RefreshTokenService localRefreshTokenService = new FakeRefreshTokenService();
        AuthService service = newAuthService(
                userAuthRepository,
                localRefreshTokenService,
                resolver,
                AuthProvider.KAKAO
        );
        OAuthLoginRequestDto requestDto = loginRequest(AuthProvider.KAKAO);

        // when: 로그인 시
        service.login(requestDto);

        // then: user_auth에 닉네임/이메일/프로필이 저장됨
        OAuthUserInfo saved = userAuthRepository.findByProviderAndProviderUserId(
                AuthProvider.KAKAO,
                "kakao-123"
        ).orElseThrow();

        assertThat(saved.getEmail()).isEqualTo("user@kakao.com");
        assertThat(saved.getNickname()).isEqualTo("홍길동");
        assertThat(saved.getProfileImageUrl()).isEqualTo("http://profile-image.com");
        assertThat(saved.getThumbnailImageUrl()).isEqualTo("http://thumb-image.com");
    }

    @Test
    @DisplayName("refresh는 토큰 검증 후 같은 userId로 access/refresh 토큰을 재발급한다.")
    void refresh_reissues_tokens_with_validated_user_id() {
        // given
        RefreshTokenService localRefreshTokenService = mock(RefreshTokenService.class);
        UUID userId = uuid(2);
        when(localRefreshTokenService.validateAndGetUserId("old-refresh")).thenReturn(userId);
        when(localRefreshTokenService.rotate(userId)).thenReturn("new-refresh");
        AuthService localAuthService = newAuthService(
                userAuthRepository,
                localRefreshTokenService,
                resolverWith(AuthProvider.DUMMY, fakeOAuthClient),
                AuthProvider.DUMMY
        );

        // when
        OAuthLoginResult result = localAuthService.refresh("old-refresh");

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertAccessTokenContainsUserId(result, userId);
        assertThat(result.refreshToken()).isEqualTo("new-refresh");
        verify(localRefreshTokenService).validateAndGetUserId("old-refresh");
        verify(localRefreshTokenService).rotate(userId);
    }

    @Test
    @DisplayName("refresh에서 토큰 검증이 실패하면 rotate를 호출하지 않고 예외를 전파한다.")
    void refresh_propagates_exception_when_validation_fails() {
        // given
        RefreshTokenService localRefreshTokenService = mock(RefreshTokenService.class);
        when(localRefreshTokenService.validateAndGetUserId("bad-refresh"))
                .thenThrow(new UnauthorizedException("유효하지 않은 Refresh Token입니다."));
        AuthService localAuthService = newAuthService(
                userAuthRepository,
                localRefreshTokenService,
                resolverWith(AuthProvider.DUMMY, fakeOAuthClient),
                AuthProvider.DUMMY
        );

        // when/then
        assertThatThrownBy(() -> localAuthService.refresh("bad-refresh"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("유효하지 않은 Refresh Token입니다.");
        verify(localRefreshTokenService).validateAndGetUserId("bad-refresh");
        verify(localRefreshTokenService, never()).rotate(any(UUID.class));
    }

    @Test
    @DisplayName("logout은 전달받은 refresh token을 삭제 요청한다.")
    void logout_deletes_plain_refresh_token() {
        // given
        RefreshTokenService localRefreshTokenService = mock(RefreshTokenService.class);
        AuthService localAuthService = newAuthService(
                userAuthRepository,
                localRefreshTokenService,
                resolverWith(AuthProvider.DUMMY, fakeOAuthClient),
                AuthProvider.DUMMY
        );

        // when
        localAuthService.logout("refresh-token");

        // then
        verify(localRefreshTokenService).deleteByPlainToken("refresh-token");
    }

    /**
     * 지정한 OAuth 공급자 기준의 기본 로그인 요청 객체를 생성한다.
     */
    private OAuthLoginRequestDto loginRequest(AuthProvider provider) {
        return OAuthLoginRequestDto.builder()
                .provider(provider)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();
    }

    /**
     * 요청 파라미터를 지정해 OAuth 로그인 요청 객체를 생성한다.
     */
    private OAuthLoginRequestDto loginRequest(AuthProvider provider, String authorizationCode, String redirectUri) {
        return OAuthLoginRequestDto.builder()
                .provider(provider)
                .authorizationCode(authorizationCode)
                .redirectUri(redirectUri)
                .build();
    }

    /**
     * providerUserId만 지정하고 나머지 프로필 필드는 비운 기본 OAuth 사용자 정보를 생성한다.
     */
    private OAuthUserInfo defaultOAuthUserInfo(String providerUserId) {
        return new OAuthUserInfo(
                providerUserId,
                null, null, null, null, null, null, null,
                false, false
        );
    }

    /**
     * 카카오 로그인 시나리오 검증에 사용하는 샘플 OAuth 사용자 정보를 생성한다.
     */
    private OAuthUserInfo kakaoOAuthUserInfo() {
        return new OAuthUserInfo(
                "kakao-123",
                "user@kakao.com",
                "홍길동",
                "http://profile-image.com",
                "http://thumb-image.com",
                Gender.MALE,
                "20~29",
                "01-15",
                true,
                true
        );
    }

    /**
     * 단일 provider-client 매핑을 가진 FakeOAuthClientResolver를 생성한다.
     */
    private FakeOAuthClientResolver resolverWith(AuthProvider provider, FakeOAuthClient client) {
        FakeOAuthClientResolver resolver = new FakeOAuthClientResolver();
        resolver.register(provider, client);
        return resolver;
    }

    /**
     * 테스트 대상 AuthServiceImpl을 기본 토큰 생성기와 함께 생성한다.
     *
     * @param userIdentityPort 사용자 식별 Port 테스트 더블
     * @param refreshTokenService 리프레시 토큰 서비스 테스트 더블
     * @param resolver OAuth 클라이언트 리졸버 테스트 더블
     * @param allowedProviders 허용할 OAuth 공급자 목록
     * @return 테스트용 AuthService 구현체
     */
    private AuthService newAuthService(
            FakeUserAuthRepository userIdentityPort,
            RefreshTokenService refreshTokenService,
            FakeOAuthClientResolver resolver,
            AuthProvider... allowedProviders
    ) {
        OAuthAllowedProvidersProperties allowedProps = new OAuthAllowedProvidersProperties();
        allowedProps.setAllowedProviders(List.of(allowedProviders));

        return new AuthServiceImpl(
                jwtAccessTokenGenerator,
                userIdentityPort,
                refreshTokenService,
                resolver,
                allowedProps
        );
    }

    /**
     * 기존 가입 사용자 시나리오를 위해 provider/providerUserId -> userId 매핑을 시드한다.
     */
    private void seedExistingUser(
            FakeUserAuthRepository userIdentityPort,
            AuthProvider provider,
            String providerUserId,
            Long userId
    ) {
        userIdentityPort.saveOAuthLink(provider, defaultOAuthUserInfo(providerUserId), uuid(userId));
    }

    /**
     * 발급된 Access Token의 subject(sub)가 기대 사용자 식별자와 일치하는지 검증한다.
     */
    private void assertAccessTokenContainsUserId(OAuthLoginResult result, UUID expectedUserId) {
        JwtAccessTokenValidator validator = new JwtAccessTokenValidator(properties);
        UUID userIdFromToken = validator.validateAndGetUserId(result.accessToken()).orElseThrow();
        assertThat(userIdFromToken).isEqualTo(expectedUserId);
    }

    private UUID uuid(long value) {
        return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", value));
    }
}
