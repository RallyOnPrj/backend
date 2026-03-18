package com.gumraze.rallyon.backend.courtManager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchRequest {
    @NotNull @Min(1)
    private Integer courtNumber;
    @NotNull @Size(min = 2, max = 2)
    private List<UUID> teamAIds;
    @NotNull @Size(min = 2, max = 2)
    private List<UUID> teamBIds;
}
