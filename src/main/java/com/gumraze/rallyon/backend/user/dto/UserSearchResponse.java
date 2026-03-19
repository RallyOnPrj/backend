package com.gumraze.rallyon.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@Getter
@AllArgsConstructor
public class UserSearchResponse {
    UUID userId;
    String nickname;
    String tag;
    String profileImageUrl;
}
