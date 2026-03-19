package com.gumraze.rallyon.backend.region.dto;

import java.util.UUID;

public record RegionDistrictResponseDto(
    UUID id,
    String name
) { }
