package com.gumraze.rallyon.backend.auth.token;

import java.util.UUID;

public interface TokenProvider {
    String generateAccessToken(UUID userId);
}
