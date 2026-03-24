package com.gumraze.rallyon.backend.user.application.port.out;

import com.gumraze.rallyon.backend.region.RegionDistrictReference;

import java.util.Optional;
import java.util.UUID;

public interface LoadRegionPort {

    Optional<RegionDistrictReference> loadDistrictReference(UUID districtId);
}
