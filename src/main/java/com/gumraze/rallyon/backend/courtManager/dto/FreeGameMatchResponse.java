package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class FreeGameMatchResponse {
    Long courtNumber;
    List<Long> teamAIds;
    List<Long> teamBIds;
    MatchStatus matchStatus;
    MatchResult matchResult;
    Boolean isActive;
}
