package com.gumraze.rallyon.backend.courtManager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoundRequest {
    @NotNull @Min(1)
    private Integer roundNumber;
    @NotNull @Valid
    private List<MatchRequest> matches;
}
