package com.gumraze.rallyon.backend.identity.adapter.out.oauth.apple.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        long expiresIn,

        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType
) {
}
