package com.gumraze.rallyon.backend.user.repository;

import com.gumraze.rallyon.backend.auth.constants.AuthProvider;
import com.gumraze.rallyon.backend.user.entity.OauthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OauthUserRepository extends JpaRepository<OauthUser, Long> {

    Optional<OauthUser> findByOauthProviderAndOauthId(AuthProvider oauthProvider, String oauthId);
}
