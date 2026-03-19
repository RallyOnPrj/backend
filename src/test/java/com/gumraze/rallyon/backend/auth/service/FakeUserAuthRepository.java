package com.gumraze.rallyon.backend.auth.service;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import com.gumraze.rallyon.backend.auth.oauth.OAuthUserInfo;
import com.gumraze.rallyon.backend.auth.port.out.UserIdentityPort;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link UserIdentityPort}의 테스트용 인메모리 구현체.
 *
 * <p>provider/providerUserId 매핑과 OAuth 프로필 정보를 Map으로 보관해
 * AuthService 테스트에서 DB 없이 사용자 식별 흐름을 검증할 수 있게 한다.</p>
 */
public class FakeUserAuthRepository implements UserIdentityPort {

    private final Map<String, UUID> userIdByKey = new HashMap<>();
    private final Map<String, OAuthUserInfo> infoByKey = new HashMap<>();
    private int createPendingUserCallCount = 0;

    /**
     * provider + providerUserId 매핑으로 내부 userId를 조회한다.
     */
    @Override
    public Optional<UUID> findUserId(AuthProvider provider, String providerUserId) {
        return Optional.ofNullable(
                userIdByKey.get(provider + ":" + providerUserId)
        );
    }

    /**
     * 증가형 시퀀스로 신규 userId를 생성한다.
     * 테스트 검증을 위해 호출 횟수를 함께 누적한다.
     */
    @Override
    public UUID createPendingUser() {
        createPendingUserCallCount++;
        return UUID.randomUUID();
    }

    /**
     * OAuth 계정 연동 정보를 새로 저장한다.
     */
    @Override
    public void saveOAuthLink(AuthProvider provider, OAuthUserInfo userInfo, UUID userId) {
        String key = provider + ":" + userInfo.getProviderUserId();
        userIdByKey.put(key, userId);
        infoByKey.put(key, userInfo);
    }

    /**
     * 기존 OAuth 계정의 프로필 정보를 최신 값으로 갱신한다.
     */
    @Override
    public void updateOAuthProfile(AuthProvider provider, OAuthUserInfo userInfo) {
        String key = provider + ":" + userInfo.getProviderUserId();
        infoByKey.put(key, userInfo);
    }

    /**
     * 저장된 OAuth 사용자 프로필 정보를 provider/providerUserId로 조회한다.
     */
    public Optional<OAuthUserInfo> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return Optional.ofNullable(infoByKey.get(provider + ":" + providerUserId));
    }

    /**
     * createPendingUser 호출 횟수를 반환한다.
     */
    public int getCreatePendingUserCallCount() {
        return createPendingUserCallCount;
    }
}
