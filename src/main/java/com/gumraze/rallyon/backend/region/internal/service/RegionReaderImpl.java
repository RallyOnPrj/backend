package com.gumraze.rallyon.backend.region.internal.service;

import com.gumraze.rallyon.backend.region.RegionDistrictReference;
import com.gumraze.rallyon.backend.region.RegionDistrictSummary;
import com.gumraze.rallyon.backend.region.RegionProvinceSummary;
import com.gumraze.rallyon.backend.region.RegionReader;
import com.gumraze.rallyon.backend.region.internal.persistence.entity.RegionDistrict;
import com.gumraze.rallyon.backend.region.internal.persistence.entity.RegionProvince;
import com.gumraze.rallyon.backend.region.internal.persistence.repository.RegionDistrictRepository;
import com.gumraze.rallyon.backend.region.internal.persistence.repository.RegionProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionReaderImpl implements RegionReader {

    private final RegionDistrictRepository regionDistrictRepository;
    private final RegionProvinceRepository regionProvinceRepository;

    @Override
    public List<RegionProvinceSummary> getProvinces() {
        return regionProvinceRepository.findAll().stream()
                .sorted(Comparator.comparing(RegionProvince::getCode))
                .map(province -> new RegionProvinceSummary(
                        province.getId(),
                        province.getName(),
                        province.getCode()
                ))
                .toList();
    }

    @Override
    public List<RegionDistrictSummary> getDistricts(UUID provinceId) {
        return regionDistrictRepository.findAllByProvinceId(provinceId).stream()
                .sorted(Comparator.comparing(RegionDistrict::getCode))
                .map(district -> new RegionDistrictSummary(
                        district.getId(),
                        district.getProvince().getId(),
                        district.getName(),
                        district.getCode()
                ))
                .toList();
    }

    @Override
    public Optional<RegionDistrictReference> findDistrictReference(UUID districtId) {
        return regionDistrictRepository.findById(districtId)
                .map(district -> new RegionDistrictReference(
                        district.getId(),
                        district.getName(),
                        district.getCode(),
                        district.getProvince().getId(),
                        district.getProvince().getName(),
                        district.getProvince().getCode()
                ));
    }
}
