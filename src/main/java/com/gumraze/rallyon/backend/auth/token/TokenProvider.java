package com.gumraze.rallyon.backend.auth.token;

public interface TokenProvider {
    String generateAccessToken(Long userId);
}
