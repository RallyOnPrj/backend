package com.gumraze.rallyon.backend.user.dto;

import java.util.UUID;

public record UserSearchResponse(
        UUID accountId,
        String nickname,
        String tag,
        String profileImageUrl
) {
}
