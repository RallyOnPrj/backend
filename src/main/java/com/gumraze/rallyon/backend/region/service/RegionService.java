package com.gumraze.rallyon.backend.region.service;

import com.gumraze.rallyon.backend.region.entity.RegionDistrict;
import com.gumraze.rallyon.backend.region.entity.RegionProvince;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegionService {

    // Id로 시/군/구가 존재하는지 조회
    boolean existsByDistrictId(UUID district_id);

    // 시/도 지역 정보 조회
    List<RegionProvince> getProvinces();

    List<RegionDistrict> getDistricts(UUID province_id);

    // 시/군/구의 지역 정보 조회
    Optional<RegionDistrict> findDistrictsById(UUID district_id);
}
