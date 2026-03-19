package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.user.entity.User;
import java.util.Optional;
import java.util.UUID;

public interface LoadUserPort {
    Optional<User> loadById(UUID userId);
}
