package com.gumraze.rallyon.backend.region;

import java.util.UUID;

public record RegionProvinceSummary(
        UUID id,
        String name,
        String code
) {
}
