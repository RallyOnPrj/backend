package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
public class FreeGameMatchResponse {
    Long courtNumber;
    List<UUID> teamAIds;
    List<UUID> teamBIds;
    MatchStatus matchStatus;
    MatchResult matchResult;
    Boolean isActive;
}
