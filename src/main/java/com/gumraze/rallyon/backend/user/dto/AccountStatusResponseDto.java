package com.gumraze.rallyon.backend.user.dto;

import com.gumraze.rallyon.backend.user.constants.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record AccountStatusResponseDto(
        @Schema(description = "인증 계정 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID identityAccountId,
        @Schema(description = "계정 상태", example = "ACTIVE")
        UserStatus status,
        @Schema(description = "프로필 생성 여부", example = "true")
        boolean hasProfile
) { }
