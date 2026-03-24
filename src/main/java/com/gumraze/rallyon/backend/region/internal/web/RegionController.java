package com.gumraze.rallyon.backend.region.internal.web;

import com.gumraze.rallyon.backend.api.region.RegionApi;
import com.gumraze.rallyon.backend.region.RegionReader;
import com.gumraze.rallyon.backend.region.dto.RegionDistrictResponseDto;
import com.gumraze.rallyon.backend.region.dto.RegionProvinceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
public class RegionController implements RegionApi {

    private final RegionReader regionReader;

    @Override
    @GetMapping("/provinces")
    public ResponseEntity<List<RegionProvinceResponseDto>> getProvinces() {
        List<RegionProvinceResponseDto> body = regionReader.getProvinces().stream()
                .map(province -> new RegionProvinceResponseDto(
                        province.id(),
                        province.name()
                ))
                .toList();

        return ResponseEntity.ok(body);
    }

    @Override
    @GetMapping("/{provinceId}/districts")
    public ResponseEntity<List<RegionDistrictResponseDto>> getDistricts(
            @PathVariable UUID provinceId
    ) {
        List<RegionDistrictResponseDto> body = regionReader.getDistricts(provinceId).stream()
                .map(district -> new RegionDistrictResponseDto(
                        district.id(),
                        district.name()
                ))
                .toList();

        return ResponseEntity.ok(body);
    }
}
