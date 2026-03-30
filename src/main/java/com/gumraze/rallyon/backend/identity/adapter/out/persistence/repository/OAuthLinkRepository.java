package com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.identity.entity.OAuthLink;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OAuthLinkRepository extends JpaRepository<OAuthLink, UUID> {

    Optional<OAuthLink> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<OAuthLink> findByAccount_IdAndProvider(UUID accountId, AuthProvider provider);

    List<OAuthLink> findByAccount_IdOrderByUpdatedAtDesc(UUID accountId);
}
