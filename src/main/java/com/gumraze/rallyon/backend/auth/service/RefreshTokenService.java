package com.gumraze.rallyon.backend.auth.service;

import java.util.UUID;

public interface RefreshTokenService {
    // 로그인 성공 시 호출되며, 기존 토큰은 폐기되고 새로운 Refresh Token이 발급됨.
    // 이후 DB에 저장
    String rotate(UUID userId);

    UUID validateAndGetUserId(String token);

    void deleteByPlainToken(String token);
}
