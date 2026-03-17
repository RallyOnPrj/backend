package com.gumraze.rallyon.backend.courtManager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class FreeGameParticipantResponse {
    private UUID participantId;
    private Long userId;
    private String displayName;
    private Gender gender;
    private Grade grade;
    private Integer ageGroup;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer assignedMatchCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer completedMatchCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer winCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer lossCount;
}
