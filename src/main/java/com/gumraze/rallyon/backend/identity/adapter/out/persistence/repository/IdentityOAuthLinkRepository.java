package com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.identity.entity.IdentityOAuthLink;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IdentityOAuthLinkRepository extends JpaRepository<IdentityOAuthLink, UUID> {

    Optional<IdentityOAuthLink> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<IdentityOAuthLink> findByIdentityAccount_IdAndProvider(UUID identityAccountId, AuthProvider provider);

    List<IdentityOAuthLink> findByIdentityAccount_IdOrderByUpdatedAtDesc(UUID identityAccountId);
}
