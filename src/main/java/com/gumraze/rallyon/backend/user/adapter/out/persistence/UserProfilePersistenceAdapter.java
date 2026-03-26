package com.gumraze.rallyon.backend.user.adapter.out.persistence;

import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.application.port.out.SaveUserProfilePort;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import com.gumraze.rallyon.backend.user.adapter.out.persistence.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserProfilePersistenceAdapter implements LoadUserProfilePort, SaveUserProfilePort {

    private final UserProfileRepository userProfileRepository;

    @Override
    public boolean existsByAccountId(UUID accountId) {
        return userProfileRepository.existsById(accountId);
    }

    @Override
    public Optional<UserProfile> loadByAccountId(UUID accountId) {
        return userProfileRepository.findByAccountId(accountId);
    }

    @Override
    public Optional<UserProfile> loadByNicknameAndTag(String nickname, String tag) {
        return userProfileRepository.findByNicknameAndTag(nickname, tag);
    }

    @Override
    public Page<UserProfile> loadByNicknameContaining(String nickname, Pageable pageable) {
        return userProfileRepository.findByNicknameContaining(nickname, pageable);
    }

    @Override
    public UserProfile save(UserProfile userProfile) {
        return userProfileRepository.save(userProfile);
    }
}
