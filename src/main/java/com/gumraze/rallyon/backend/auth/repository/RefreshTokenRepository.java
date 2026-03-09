package com.gumraze.rallyon.backend.auth.repository;

import com.gumraze.rallyon.backend.auth.entity.RefreshToken;
import com.gumraze.rallyon.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByUser(User user);
}
