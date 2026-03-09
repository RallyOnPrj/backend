package com.gumraze.rallyon.backend.user.service;

import com.gumraze.rallyon.backend.user.repository.JpaUserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserAuthNicknameProvider implements UserNicknameProvider {

    private final JpaUserAuthRepository userAuthRepository;

    @Override
    public Optional<String> findNicknameByUserId(Long userId) {
        return userAuthRepository
                .findFirstByUser_IdOrderByUpdatedAtDesc(userId)
                .map(userAuth -> userAuth.getNickname())
                .filter(nickname -> nickname != null && !nickname.isBlank());
    }

}
