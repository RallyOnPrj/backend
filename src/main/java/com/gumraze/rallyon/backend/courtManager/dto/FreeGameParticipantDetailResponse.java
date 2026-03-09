package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
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
    private Grade grade;
    private Integer ageGroup;
}
