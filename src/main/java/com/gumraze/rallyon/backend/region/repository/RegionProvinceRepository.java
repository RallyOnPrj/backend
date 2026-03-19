package com.gumraze.rallyon.backend.region.repository;

import com.gumraze.rallyon.backend.region.entity.RegionProvince;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RegionProvinceRepository extends JpaRepository<RegionProvince, UUID> {
    /***
     * id 오름차순으로 정렬된 시/도 데이터 조회
     */
    List<RegionProvince> findAllByOrderById();
}
