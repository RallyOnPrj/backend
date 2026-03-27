package com.gumraze.rallyon.backend.region.internal.persistence.repository;

import com.gumraze.rallyon.backend.region.internal.persistence.entity.RegionProvince;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RegionProvinceRepository extends JpaRepository<RegionProvince, UUID> {
}
