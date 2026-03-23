package com.gumraze.rallyon.backend.user.application.port.out;

import com.gumraze.rallyon.backend.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface LoadIdentityUserPort {

    Optional<User> loadById(UUID userId);
}
