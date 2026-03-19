package com.gumraze.rallyon.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Builder
@AllArgsConstructor
@Getter
public class UserProfileCreateResponseDto {
        UUID userId;
}
