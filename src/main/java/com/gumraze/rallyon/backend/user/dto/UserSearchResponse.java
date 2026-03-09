package com.gumraze.rallyon.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class UserSearchResponse {
    Long userId;
    String nickname;
    String tag;
    String profileImageUrl;
}
