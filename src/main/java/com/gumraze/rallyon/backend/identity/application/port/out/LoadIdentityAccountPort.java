package com.gumraze.rallyon.backend.identity.application.port.out;

import com.gumraze.rallyon.backend.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface LoadIdentityAccountPort {

    Optional<User> loadById(UUID userId);
}
