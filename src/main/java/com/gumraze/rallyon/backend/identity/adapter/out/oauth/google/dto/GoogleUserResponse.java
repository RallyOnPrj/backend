package com.gumraze.rallyon.backend.identity.adapter.out.oauth.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserResponse(
        @JsonProperty("sub") String sub,
        @JsonProperty("email") String email,
        @JsonProperty("email_verified") Boolean emailVerified,
        @JsonProperty("name") String name,
        @JsonProperty("picture") String picture
) {
}
