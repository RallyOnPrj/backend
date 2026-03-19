package com.gumraze.rallyon.backend.user.service;

import java.util.Optional;
import java.util.UUID;

public interface UserNicknameProvider {
    Optional<String> findNicknameByUserId(UUID userId);
}
