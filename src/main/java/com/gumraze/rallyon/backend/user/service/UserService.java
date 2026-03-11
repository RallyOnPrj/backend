package com.gumraze.rallyon.backend.user.service;

import com.gumraze.rallyon.backend.user.dto.UserMeResponse;
import com.gumraze.rallyon.backend.user.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findById(Long id);

    UserMeResponse getUserMe(Long userId);
}
