package com.gumraze.rallyon.backend.region.internal.persistence.repository;

import com.gumraze.rallyon.backend.region.internal.persistence.entity.RegionDistrict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegionDistrictRepository extends JpaRepository<RegionDistrict, UUID> {

    List<RegionDistrict> findAllByProvinceId(UUID provinceId);
}
