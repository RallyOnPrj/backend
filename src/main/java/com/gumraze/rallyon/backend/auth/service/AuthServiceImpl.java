package com.gumraze.rallyon.backend.auth.service;

import com.gumraze.rallyon.backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.rallyon.backend.auth.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.auth.oauth.OAuthClientResolver;
import com.gumraze.rallyon.backend.auth.oauth.OAuthUserInfo;
import com.gumraze.rallyon.backend.auth.port.out.UserIdentityPort;
import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * OAuth 로그인/리프레시/로그아웃에 대한 Auth 도메인 서비스 구현체.
 *
 * <p>사용자 식별·연동 정보의 저장/조회는 {@link UserIdentityPort}를 통해 수행하며,
 * 토큰 발급/검증 흐름에만 집중한다.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final JwtAccessTokenGenerator jwtAccessTokenGenerator;
    private final UserIdentityPort userIdentityPort;
    private final RefreshTokenService refreshTokenService;
    private final OAuthClientResolver oAuthClientResolver;
    private final OAuthAllowedProvidersProperties allowedProviders;

    /**
     * OAuth 공급자 로그인 결과를 바탕으로 사용자 식별 후 Access/Refresh 토큰을 발급한다.
     *
     * @param request OAuth 로그인 요청 정보
     * @return 로그인 결과(내부 userId, accessToken, refreshToken)
     */
    @Override
    public OAuthLoginResult login(OAuthLoginRequestDto request) {
        if (!allowedProviders.getAllowedProviders().contains(request.getProvider())) {
            throw new IllegalArgumentException("허용되지 않는 provider: " + request.getProvider());
        }

        OAuthUserInfo userInfo = oAuthClientResolver
                .resolve(request.getProvider())
                .getOAuthUserInfo(request.getAuthorizationCode(), request.getRedirectUri());

        UUID userId = userIdentityPort
                .findUserId(request.getProvider(), userInfo.getProviderUserId())
                .orElseGet(() -> {
                    UUID newUserId = userIdentityPort.createPendingUser();
                    userIdentityPort.saveOAuthLink(request.getProvider(), userInfo, newUserId);
                    return newUserId;
                });

        userIdentityPort.updateOAuthProfile(request.getProvider(), userInfo);

        String accessToken = jwtAccessTokenGenerator.generateAccessToken(userId);
        String refreshToken = refreshTokenService.rotate(userId);

        return new OAuthLoginResult(userId, accessToken, refreshToken);
    }

    /**
     * Refresh Token을 검증해 사용자 식별자를 얻고 새 Access/Refresh 토큰을 발급한다.
     *
     * @param refreshToken 평문 Refresh Token
     * @return 재발급 결과(내부 userId, accessToken, refreshToken)
     */
    @Override
    public OAuthLoginResult refresh(String refreshToken) {
        UUID userId = refreshTokenService.validateAndGetUserId(refreshToken);
        String newAccessToken = jwtAccessTokenGenerator.generateAccessToken(userId);
        String newRefreshToken = refreshTokenService.rotate(userId);

        return new OAuthLoginResult(userId, newAccessToken, newRefreshToken);
    }

    /**
     * 주어진 Refresh Token을 무효화한다.
     *
     * @param refreshToken 평문 Refresh Token
     */
    @Override
    public void logout(String refreshToken) {
        refreshTokenService.deleteByPlainToken(refreshToken);
    }
}
