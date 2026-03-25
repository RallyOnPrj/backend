package com.gumraze.rallyon.backend.courtManager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record MatchRequest(
        @NotNull @Min(1) Integer courtNumber,
        @NotNull @Size(min = 2, max = 2) List<UUID> teamAIds,
        @NotNull @Size(min = 2, max = 2) List<UUID> teamBIds
) {}
