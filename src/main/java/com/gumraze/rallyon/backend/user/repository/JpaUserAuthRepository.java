package com.gumraze.rallyon.backend.user.repository;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import com.gumraze.rallyon.backend.user.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaUserAuthRepository extends JpaRepository<UserAuth, UUID> {

    Optional<UserAuth> findByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );

    // 사용자의 인증 정보들 중에서 가장 최근에 업데이트된 데이터를 하나 찾는 메서드
    Optional<UserAuth> findFirstByUser_IdOrderByUpdatedAtDesc(UUID userId);
}
