package com.gumraze.rallyon.backend.user.dto;

import java.util.UUID;

public record UserSearchResponse(
        UUID identityAccountId,
        String nickname,
        String tag,
        String profileImageUrl
) {
}
