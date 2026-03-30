package com.gumraze.rallyon.backend.region;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionReader {

    List<RegionProvinceSummary> getProvinces();

    List<RegionDistrictSummary> getDistricts(UUID provinceId);

    Optional<RegionDistrictReference> findDistrictReference(UUID districtId);
}
