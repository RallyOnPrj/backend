package com.gumraze.rallyon.backend.courtManager.domain.assignment;
import java.util.List;

public record RoundAssignment(
        Integer roundNumber,
        List<CourtAssignment> courts
) {
}
