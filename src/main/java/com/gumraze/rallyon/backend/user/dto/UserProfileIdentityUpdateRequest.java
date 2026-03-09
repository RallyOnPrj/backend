package com.gumraze.rallyon.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class UserProfileIdentityUpdateRequest {
    String nickname;
    String tag;
}
