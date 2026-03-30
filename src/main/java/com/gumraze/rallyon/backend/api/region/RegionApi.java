package com.gumraze.rallyon.backend.api.region;

import com.gumraze.rallyon.backend.api.common.ApiBadRequestResponse;
import com.gumraze.rallyon.backend.api.common.ApiServerErrorResponse;
import com.gumraze.rallyon.backend.region.dto.RegionDistrictResponseDto;
import com.gumraze.rallyon.backend.region.dto.RegionProvinceResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Tag(name = "Regions", description = "지역 조회 API")
public interface RegionApi {

    @Operation(
            summary = "시/도 조회",
            description = "전체 시/도 목록을 조회합니다.",
            security = {}
    )
    @ApiServerErrorResponse
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "시/도 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegionProvinceResponseDto.class)
                    )
            )
    })
    ResponseEntity<List<RegionProvinceResponseDto>> getProvinces();

    @Operation(
            summary = "시/군/구 조회",
            description = "시/도 ID로 시/군/구 목록을 조회합니다.",
            security = {}
    )
    @ApiBadRequestResponse
    @ApiServerErrorResponse
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "시/군/구 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RegionDistrictResponseDto.class)
                    )
            )
    })
    ResponseEntity<List<RegionDistrictResponseDto>> getDistricts(
            @Parameter(description = "시/도 ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            UUID provinceId
    );
}
