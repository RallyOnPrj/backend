package com.gumraze.rallyon.backend.identity.authorizationserver.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthTokenResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("refresh_token")
        String refreshToken,
        @JsonProperty("token_type")
        String tokenType,
        @JsonProperty("expires_in")
        Long expiresIn,
        @JsonProperty("scope")
        String scope,
        @JsonProperty("id_token")
        String idToken
) {
}
