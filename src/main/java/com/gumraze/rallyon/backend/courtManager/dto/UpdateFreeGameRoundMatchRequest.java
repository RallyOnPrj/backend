package com.gumraze.rallyon.backend.courtManager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateFreeGameRoundMatchRequest(
        @NotEmpty @Valid List<RoundRequest> rounds
) {}
