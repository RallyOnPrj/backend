package com.gumraze.rallyon.backend.user.service;

import java.util.Optional;

public interface UserNicknameProvider {
    Optional<String> findNicknameByUserId(Long userId);
}
