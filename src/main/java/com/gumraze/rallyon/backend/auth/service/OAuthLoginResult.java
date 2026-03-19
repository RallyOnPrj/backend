package com.gumraze.rallyon.backend.auth.service;

import java.util.UUID;

public record OAuthLoginResult(
        UUID userId,
        String accessToken,
        String refreshToken
) { }
