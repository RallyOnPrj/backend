package com.gumraze.rallyon.backend.user.dto;

public record UserProfileDefaultsResponse(
        String suggestedNickname,
        boolean hasSuggestedNickname
) {
}
