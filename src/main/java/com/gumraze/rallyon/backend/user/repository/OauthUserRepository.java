package com.gumraze.rallyon.backend.user.repository;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import com.gumraze.rallyon.backend.user.entity.OauthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OauthUserRepository extends JpaRepository<OauthUser, UUID> {

    Optional<OauthUser> findByOauthProviderAndOauthId(AuthProvider oauthProvider, String oauthId);
}
