package com.gumraze.rallyon.backend.region.controller;

import com.gumraze.rallyon.backend.api.region.RegionApi;
import com.gumraze.rallyon.backend.region.dto.RegionDistrictResponseDto;
import com.gumraze.rallyon.backend.region.dto.RegionProvinceResponseDto;
import com.gumraze.rallyon.backend.region.service.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/regions")
public class RegionController implements RegionApi {

    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }


    @Override
    @GetMapping(value = "/provinces")
    public ResponseEntity<List<RegionProvinceResponseDto>> getProvinces() {
        List<RegionProvinceResponseDto> body = regionService.getProvinces().stream()
                .map(p -> new RegionProvinceResponseDto(
                        p.getId(),
                        p.getName()
                ))
                .toList();
        return ResponseEntity.ok(body);
    }

    @Override
    @GetMapping(value = "/{provinceId}/districts")
    public ResponseEntity<List<RegionDistrictResponseDto>> getDistricts(
            @PathVariable UUID provinceId
    ) {
        List<RegionDistrictResponseDto> body = regionService.getDistricts(provinceId).stream()
                .map(d -> new RegionDistrictResponseDto(
                        d.getId(),
                        d.getName()
                ))
                .toList();
        return ResponseEntity.ok(body);
    }
}
