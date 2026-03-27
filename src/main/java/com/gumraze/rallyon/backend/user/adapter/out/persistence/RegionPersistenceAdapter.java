package com.gumraze.rallyon.backend.user.adapter.out.persistence;

import com.gumraze.rallyon.backend.region.RegionDistrictReference;
import com.gumraze.rallyon.backend.region.RegionReader;
import com.gumraze.rallyon.backend.user.application.port.out.LoadRegionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RegionPersistenceAdapter implements LoadRegionPort {

    private final RegionReader regionReader;

    @Override
    public Optional<RegionDistrictReference> loadDistrictReference(UUID districtId) {
        return regionReader.findDistrictReference(districtId);
    }
}
