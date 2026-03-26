package com.gumraze.rallyon.backend.user.adapter.out.persistence.repository;

import com.gumraze.rallyon.backend.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByAccountId(UUID accountId);

    Page<UserProfile> findByNicknameContaining(String nickname, Pageable pageable);

    Optional<UserProfile> findByNicknameAndTag(String nickname, String tag);
}
