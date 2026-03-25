package com.gumraze.rallyon.backend.identity.domain;

import com.gumraze.rallyon.backend.user.constants.Gender;

public record OAuthUserInfo(
        String providerUserId,
        String email,
        String nickname,
        String profileImageUrl,
        String thumbnailImageUrl,
        Gender gender,
        String ageRange,
        String birthday,
        Boolean emailVerified,
        Boolean phoneNumberVerified
) {
}
