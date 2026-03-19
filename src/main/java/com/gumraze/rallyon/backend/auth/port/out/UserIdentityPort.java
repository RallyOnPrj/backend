package com.gumraze.rallyon.backend.auth.port.out;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import com.gumraze.rallyon.backend.auth.oauth.OAuthUserInfo;

import java.util.Optional;
import java.util.UUID;

/**
 * Auth 도메인이 사용자 식별/연동 정보를 조회·갱신하기 위해 사용하는 Outbound Port.
 *
 * <p>Auth 서비스는 이 인터페이스만 의존하고, 실제 저장소 접근(JPA)은 Adapter가 담당한다.</p>
 */
public interface UserIdentityPort {

    /**
     * OAuth 공급자 정보로 내부 사용자 식별자를 조회한다.
     *
     * @param provider OAuth 공급자
     * @param providerUserId 공급자에서 발급한 사용자 식별자
     * @return 매핑된 내부 사용자 식별자(Optional)
     */
    Optional<UUID> findUserId(AuthProvider provider, String providerUserId);

    /**
     * 신규 사용자를 PENDING 상태로 생성하고 식별자를 반환한다.
     *
     * @return 생성된 내부 사용자 식별자
     */
    UUID createPendingUser();

    /**
     * OAuth 계정과 내부 사용자를 연결하고 초기 프로필 정보를 저장한다.
     *
     * @param provider OAuth 공급자
     * @param userInfo OAuth 사용자 정보
     * @param userId 내부 사용자 식별자
     */
    void saveOAuthLink(AuthProvider provider, OAuthUserInfo userInfo, UUID userId);

    /**
     * 기존 OAuth 연동 정보의 프로필 필드를 최신 값으로 갱신한다.
     *
     * @param provider OAuth 공급자
     * @param userInfo OAuth 사용자 정보
     */
    void updateOAuthProfile(AuthProvider provider, OAuthUserInfo userInfo);
}
