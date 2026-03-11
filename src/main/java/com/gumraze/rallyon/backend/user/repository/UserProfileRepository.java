package com.gumraze.rallyon.backend.user.repository;

import com.gumraze.rallyon.backend.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);

    Page<UserProfile> findByNicknameContaining(String nickname, Pageable pageable);

    Optional<UserProfile> findByNicknameAndTag(String nickname, String tag);
}