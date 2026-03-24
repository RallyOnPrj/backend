package com.gumraze.rallyon.backend.user.application.port.out;

import com.gumraze.rallyon.backend.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface LoadUserProfilePort {

    boolean existsByUserId(UUID userId);

    Optional<UserProfile> loadByUserId(UUID userId);

    Optional<UserProfile> loadByNicknameAndTag(String nickname, String tag);

    Page<UserProfile> loadByNicknameContaining(String nickname, Pageable pageable);
}
