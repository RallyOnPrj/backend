package com.gumraze.rallyon.backend.region;

import java.util.UUID;

public record RegionDistrictSummary(
        UUID id,
        UUID provinceId,
        String name,
        String code
) {
}
