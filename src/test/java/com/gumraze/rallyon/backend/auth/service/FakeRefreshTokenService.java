package com.gumraze.rallyon.backend.auth.service;

import java.util.UUID;

public class FakeRefreshTokenService implements RefreshTokenService{
    @Override
    public String rotate(UUID userId) {
        return "fake-refresh-token" + userId;
    }

    @Override
    public UUID validateAndGetUserId(String refreshToken) {
        return UUID.fromString("00000000-0000-0000-0000-000000000000");
    }

    @Override
    public void deleteByPlainToken(String token) {
        
    }
}
