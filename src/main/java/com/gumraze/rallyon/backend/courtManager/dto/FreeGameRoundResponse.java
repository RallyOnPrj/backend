package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;

import java.util.List;

public record FreeGameRoundResponse(
        Integer roundNumber,
        RoundStatus roundStatus,
        List<FreeGameMatchResponse> matches
) {}
