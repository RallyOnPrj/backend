package com.gumraze.rallyon.backend.user.service;

import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.dto.UserMeResponse;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import com.gumraze.rallyon.backend.user.repository.UserProfileRepository;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public Optional<User> findById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    public UserMeResponse getUserMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserProfile profile = null;
        if (user.getStatus() == UserStatus.ACTIVE) {
            profile = userProfileRepository.findByUserId(userId).orElse(null);
        }

        return UserMeResponse.from(user, profile);
    }
}
