package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FreeGameParticipantDetailResponse {
    private Long gameId;
    private Long participantId;
    private Long userId;
    private String displayName;
    private Gender gender;
    private Integer ageGroup;
}
