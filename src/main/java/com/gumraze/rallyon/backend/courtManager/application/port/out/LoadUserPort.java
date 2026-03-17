package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.user.entity.User;
import java.util.Optional;

public interface LoadUserPort {
    Optional<User> loadById(Long userId);
}
