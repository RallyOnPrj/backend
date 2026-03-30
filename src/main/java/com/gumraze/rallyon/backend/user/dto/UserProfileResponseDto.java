package com.gumraze.rallyon.backend.user.dto;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.UserStatus;

import java.time.LocalDateTime;

public record UserProfileResponseDto(
        UserStatus status,
        String nickname,
        String tag,
        String profileImageUrl,
        Gender gender,
        LocalDateTime birth,
        boolean birthVisible,
        Grade regionalGrade,
        Grade nationalGrade,
        String districtName,
        String provinceName,
        LocalDateTime tagChangedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
