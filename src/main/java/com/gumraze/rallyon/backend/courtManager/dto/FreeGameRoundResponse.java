package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class FreeGameRoundResponse {
    Integer roundNumber;
    RoundStatus roundStatus;
    List<FreeGameMatchResponse> matches;
}
