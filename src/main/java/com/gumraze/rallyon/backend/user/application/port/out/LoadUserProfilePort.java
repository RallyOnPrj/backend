package com.gumraze.rallyon.backend.user.application.port.out;

import com.gumraze.rallyon.backend.user.entity.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface LoadUserProfilePort {

    boolean existsByIdentityAccountId(UUID identityAccountId);

    Optional<UserProfile> loadByIdentityAccountId(UUID identityAccountId);

    Optional<UserProfile> loadByNicknameAndTag(String nickname, String tag);

    Page<UserProfile> loadByNicknameContaining(String nickname, Pageable pageable);
}
