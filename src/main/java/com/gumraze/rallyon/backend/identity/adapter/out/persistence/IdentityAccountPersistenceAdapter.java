package com.gumraze.rallyon.backend.identity.adapter.out.persistence;

import com.gumraze.rallyon.backend.identity.application.port.out.LoadIdentityAccountPort;
import com.gumraze.rallyon.backend.identity.application.port.out.SaveIdentityAccountPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadIdentityUserPort;
import com.gumraze.rallyon.backend.user.entity.User;
import com.gumraze.rallyon.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdentityAccountPersistenceAdapter implements LoadIdentityAccountPort, SaveIdentityAccountPort, LoadIdentityUserPort {

    private final UserRepository userRepository;

    @Override
    public Optional<User> loadById(UUID userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
}
