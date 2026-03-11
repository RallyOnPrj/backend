package com.gumraze.rallyon.backend.auth.service;

import com.gumraze.rallyon.backend.auth.dto.OAuthLoginRequestDto;

public interface AuthService {

    OAuthLoginResult login(OAuthLoginRequestDto request);

    OAuthLoginResult refresh(String refreshToken);

    void logout(String refreshToken);
}
