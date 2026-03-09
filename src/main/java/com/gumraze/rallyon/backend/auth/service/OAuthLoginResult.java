package com.gumraze.rallyon.backend.auth.service;

public record OAuthLoginResult(
        Long userId,
        String accessToken,
        String refreshToken
) { }
