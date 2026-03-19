package com.gumraze.rallyon.backend.auth.adapter.out.persistence;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import com.gumraze.rallyon.backend.auth.oauth.OAuthUserInfo;
import com.gumraze.rallyon.backend.auth.port.out.UserIdentityPort;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserAuth;
import com.gumraze.rallyon.backend.user.repository.JpaUserAuthRepository;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link UserIdentityPort}의 JPA 기반 Adapter 구현체.
 *
 * <p>Auth 도메인에서 선언한 포트 호출을 Spring Data JPA 조회/저장 연산으로 변환한다.</p>
 */
@Component
@RequiredArgsConstructor
@Transactional
public class UserIdentityJpaAdapter implements UserIdentityPort {

    private final UserRepository userRepository;
    private final JpaUserAuthRepository userAuthRepository;

    /**
     * provider + providerUserId 조합으로 내부 사용자 식별자를 조회한다.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> findUserId(AuthProvider provider, String providerUserId) {
        return userAuthRepository.findByProviderAndProviderUserId(provider, providerUserId)
                .map(userAuth -> userAuth.getUser().getId());
    }

    /**
     * PENDING 상태의 신규 사용자를 생성하고 식별자를 반환한다.
     */
    @Override
    public UUID createPendingUser() {
        User user = userRepository.save(
                User.builder()
                        .status(UserStatus.PENDING)
                        .build()
        );
        return user.getId();
    }

    /**
     * OAuth 계정 연동 정보를 최초 생성하고, 프로필 필드를 함께 저장한다.
     */
    @Override
    public void saveOAuthLink(AuthProvider provider, OAuthUserInfo userInfo, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();

        UserAuth userAuth = UserAuth.builder()
                .user(user)
                .provider(provider)
                .providerUserId(userInfo.getProviderUserId())
                .build();

        applyProfile(userAuth, userInfo);
        userAuthRepository.save(userAuth);
    }

    /**
     * 기존 OAuth 연동 레코드가 있으면 최신 프로필 값으로 갱신한다.
     */
    @Override
    public void updateOAuthProfile(AuthProvider provider, OAuthUserInfo userInfo) {
        userAuthRepository.findByProviderAndProviderUserId(provider, userInfo.getProviderUserId())
                .ifPresent(userAuth -> applyProfile(userAuth, userInfo));
    }

    /**
     * OAuth 사용자 정보의 프로필 관련 필드를 UserAuth 엔티티에 반영한다.
     */
    private void applyProfile(UserAuth userAuth, OAuthUserInfo userInfo) {
        userAuth.setEmail(userInfo.getEmail());
        userAuth.setNickname(userInfo.getNickname());
        userAuth.setProfileImageUrl(userInfo.getProfileImageUrl());
        userAuth.setThumbnailImageUrl(userInfo.getThumbnailImageUrl());
        userAuth.setGender(userInfo.getGender());
        userAuth.setAgeRange(userInfo.getAgeRange());
        userAuth.setBirthday(userInfo.getBirthday());
        userAuth.setIsEmailVerified(userInfo.getEmailVerified());
        userAuth.setIsPhoneNumberVerified(userInfo.getPhoneNumberVerified());
        userAuth.setUpdatedAt(LocalDateTime.now());
    }
}
