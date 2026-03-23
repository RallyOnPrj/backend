package com.gumraze.rallyon.backend.region;

import java.util.UUID;

public record RegionDistrictReference(
        UUID districtId,
        String districtName,
        String districtCode,
        UUID provinceId,
        String provinceName,
        String provinceCode
) {
}
