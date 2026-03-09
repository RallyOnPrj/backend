package com.gumraze.rallyon.backend.user.dto;

public record UserProfilePrefillResponseDto(
        String suggestedNickname,
        boolean hasOauthNickname
) { }
