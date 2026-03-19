package com.gumraze.rallyon.backend.user.service;

import com.gumraze.rallyon.backend.user.dto.UserMeResponse;
import com.gumraze.rallyon.backend.user.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Optional<User> findById(UUID id);

    UserMeResponse getUserMe(UUID userId);
}
