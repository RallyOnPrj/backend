package com.gumraze.rallyon.backend.courtManager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RoundRequest(
        @NotNull @Min(1) Integer roundNumber,
        @NotNull @Valid List<MatchRequest> matches
) {}
