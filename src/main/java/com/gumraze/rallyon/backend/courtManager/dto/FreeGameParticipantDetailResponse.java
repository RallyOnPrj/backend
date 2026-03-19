package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class FreeGameParticipantDetailResponse {
    private UUID gameId;
    private UUID participantId;
    private UUID userId;
    private String displayName;
    private Gender gender;
    private Grade grade;
    private Integer ageGroup;
}
