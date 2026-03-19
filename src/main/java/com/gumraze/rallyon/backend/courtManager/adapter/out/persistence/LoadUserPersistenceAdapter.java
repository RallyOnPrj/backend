package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadUserPort;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadUserPersistenceAdapter implements LoadUserPort {

    private final UserRepository userRepository;

    @Override
    public Optional<User> loadById(UUID userId) {
        return userRepository.findById(userId);
    }
}
