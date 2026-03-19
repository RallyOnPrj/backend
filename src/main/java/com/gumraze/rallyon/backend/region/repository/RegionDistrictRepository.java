package com.gumraze.rallyon.backend.region.repository;

import com.gumraze.rallyon.backend.region.entity.RegionDistrict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RegionDistrictRepository extends JpaRepository<RegionDistrict, UUID> {
    // province Id로 district 조회
    List<RegionDistrict> findAllByProvinceId(UUID province_id);
}
