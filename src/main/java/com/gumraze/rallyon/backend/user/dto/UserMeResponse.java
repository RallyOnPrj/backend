package com.gumraze.rallyon.backend.user.dto;

import com.gumraze.rallyon.backend.user.constants.UserStatus;

public record UserMeResponse(
        UserStatus status,
        String profileImageUrl,
        String nickname
) {
}
