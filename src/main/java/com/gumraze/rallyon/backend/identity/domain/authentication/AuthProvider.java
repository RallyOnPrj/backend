package com.gumraze.rallyon.backend.identity.domain.authentication;

public enum AuthProvider {
    KAKAO,
    // Retained for historical oauth rows. Disable via config instead of deleting this enum value.
    GOOGLE,
    DUMMY
}
